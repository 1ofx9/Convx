package com.convx.storage.data

import com.convx.storage.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NavidromeFileSource(
    val client: OkHttpClient = OkHttpClient.Builder()
        .dispatcher(Dispatcher().apply {
            maxRequests = 32
            maxRequestsPerHost = 24
        })
        .connectionPool(ConnectionPool(32, 5, TimeUnit.MINUTES))
        .followRedirects(true)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build(),
) {

    fun baseUrl(server: NetworkServer): String {
        var addr = server.address.trim()
        if (!addr.startsWith("http://", ignoreCase = true) && !addr.startsWith("https://", ignoreCase = true)) {
            addr = "http://$addr"
        }
        if (!addr.endsWith("/")) {
            addr = "$addr/"
        }
        return addr
    }

    fun authQuery(server: NetworkServer): String {
        val salt = UUID.randomUUID().toString().replace("-", "").take(8)
        val raw = server.credentials.password + salt
        val md = MessageDigest.getInstance("MD5")
        val token = md.digest(raw.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
        val user = java.net.URLEncoder.encode(server.credentials.username, "UTF-8")
        return "u=$user&t=$token&s=$salt&v=1.16.1&c=Convx&f=json"
    }

    suspend fun test(server: NetworkServer) = withContext(Dispatchers.IO) {
        val url = "${baseUrl(server)}rest/ping.view?${authQuery(server)}".toHttpUrl()
        val request = Request.Builder().url(url).build()
        client.newCall(request).await().use { response ->
            if (!response.isSuccessful) throw StorageException(StorageError.CONNECTION)
            val json = JSONObject(response.body?.string() ?: "{}")
            val subResponse = json.optJSONObject("subsonic-response") ?: throw StorageException(StorageError.INVALID_RESPONSE)
            val status = subResponse.optString("status")
            if (status != "ok") {
                val error = subResponse.optJSONObject("error")
                val code = error?.optInt("code", 0) ?: 0
                if (code == 40 || code == 41) throw StorageException(StorageError.AUTHENTICATION)
                throw StorageException(StorageError.AUTHENTICATION)
            }
        }
    }

    suspend fun scan(server: NetworkServer, maxTracks: Int = 10_000): NetworkLibrary = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<NetworkTrack>()
        val existingTrackIds = mutableSetOf<String>()
        var offset = 0
        val pageSize = 500
        var truncated = false
        val semaphore = Semaphore(16)

        while (tracks.size < maxTracks) {
            val url = "${baseUrl(server)}rest/getAlbumList2.view?type=alphabeticalByName&size=$pageSize&offset=$offset&${authQuery(server)}".toHttpUrl()
            val request = Request.Builder().url(url).build()
            val albumIds = client.newCall(request).await().use { response ->
                if (!response.isSuccessful) throw StorageException(StorageError.CONNECTION)
                val json = JSONObject(response.body?.string() ?: "{}")
                val subResponse = json.optJSONObject("subsonic-response") ?: throw StorageException(StorageError.INVALID_RESPONSE)
                val albumList = subResponse.optJSONObject("albumList2")?.optJSONArray("album")
                val ids = mutableListOf<String>()
                if (albumList != null) {
                    for (i in 0 until albumList.length()) {
                        val albumObj = albumList.optJSONObject(i) ?: continue
                        val id = albumObj.optString("id")
                        if (id.isNotEmpty()) ids.add(id)
                    }
                }
                ids
            }

            if (albumIds.isEmpty()) break

            // Concurrently fetch all albums in parallel using semaphore
            val albumTrackLists = coroutineScope {
                albumIds.map { albumId ->
                    async {
                        semaphore.withPermit {
                            fetchAlbumTracks(server, albumId)
                        }
                    }
                }.awaitAll()
            }

            for (albumTracks in albumTrackLists) {
                for (track in albumTracks) {
                    if (tracks.size >= maxTracks) {
                        truncated = true
                        break
                    }
                    if (existingTrackIds.add(track.id)) {
                        tracks.add(track)
                    }
                }
                if (truncated) break
            }

            offset += pageSize
            if (albumIds.size < pageSize) break
        }

        // Fetch user playlists and index any missing playlist tracks
        val playlists = fetchPlaylists(server, tracks, existingTrackIds)

        NetworkLibrary(tracks = tracks, truncated = truncated, playlists = playlists)
    }

    private suspend fun fetchAlbumTracks(
        server: NetworkServer,
        albumId: String,
    ): List<NetworkTrack> {
        val albumUrl = "${baseUrl(server)}rest/getAlbum.view?id=$albumId&${authQuery(server)}".toHttpUrl()
        val albumReq = Request.Builder().url(albumUrl).build()
        return try {
            client.newCall(albumReq).await().use { response ->
                if (!response.isSuccessful) return emptyList()
                val json = JSONObject(response.body?.string() ?: "{}")
                val subResponse = json.optJSONObject("subsonic-response") ?: return emptyList()
                val albumObj = subResponse.optJSONObject("album") ?: return emptyList()
                val albumName = albumObj.optString("name").takeIf { it.isNotBlank() }
                val artistName = albumObj.optString("artist").takeIf { it.isNotBlank() }
                val coverArtId = albumObj.optString("coverArt").takeIf { it.isNotBlank() }
                val year = albumObj.optInt("year", -1).takeIf { it > 0 }
                val songArray = albumObj.optJSONArray("song") ?: return emptyList()

                val list = ArrayList<NetworkTrack>(songArray.length())
                for (s in 0 until songArray.length()) {
                    val songObj = songArray.optJSONObject(s) ?: continue
                    val songId = songObj.optString("id")
                    if (songId.isEmpty()) continue
                    val trackUri = NetworkMediaUri.create(server.id, songId)

                    val title = songObj.optString("title", "Unknown Title")
                    val songArtist = songObj.optString("artist").takeIf { it.isNotBlank() } ?: artistName
                    val songAlbum = songObj.optString("album").takeIf { it.isNotBlank() } ?: albumName
                    val songCover = songObj.optString("coverArt").takeIf { it.isNotBlank() } ?: coverArtId
                    val duration = songObj.optInt("duration", -1)
                    val trackNumber = songObj.optInt("track", -1).takeIf { it > 0 }
                    val songYear = songObj.optInt("year", -1).takeIf { it > 0 } ?: year
                    val size = songObj.optLong("size", -1L)

                    val artworkUrl = songCover?.let {
                        "${baseUrl(server)}rest/getCoverArt.view?id=$it&size=800&${authQuery(server)}"
                    }

                    list.add(
                        NetworkTrack(
                            id = trackUri,
                            title = title,
                            artist = songArtist,
                            album = songAlbum,
                            artwork = artworkUrl,
                            size = size,
                            duration = duration,
                            year = songYear,
                            trackNumber = trackNumber,
                            albumId = "navidrome_album_${server.id}_$albumId",
                            artistId = "navidrome_artist_${server.id}_${(songArtist ?: "").hashCode()}",
                        ),
                    )
                }
                list
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchPlaylists(
        server: NetworkServer,
        tracks: MutableList<NetworkTrack>,
        existingTrackIds: MutableSet<String>,
    ): List<NetworkPlaylist> = withContext(Dispatchers.IO) {
        val result = mutableListOf<NetworkPlaylist>()
        try {
            val url = "${baseUrl(server)}rest/getPlaylists.view?${authQuery(server)}".toHttpUrl()
            val request = Request.Builder().url(url).build()
            val playlistSummaries = client.newCall(request).await().use { response ->
                if (!response.isSuccessful) return@use emptyList<Triple<String, String, String?>>()
                val json = JSONObject(response.body?.string() ?: "{}")
                val subResponse = json.optJSONObject("subsonic-response") ?: return@use emptyList()
                val playlistArray = subResponse.optJSONObject("playlists")?.optJSONArray("playlist") ?: return@use emptyList()
                val list = mutableListOf<Triple<String, String, String?>>()
                for (i in 0 until playlistArray.length()) {
                    val p = playlistArray.optJSONObject(i) ?: continue
                    val id = p.optString("id")
                    val name = p.optString("name")
                    val cover = p.optString("coverArt").takeIf { it.isNotBlank() }
                    if (id.isNotEmpty() && name.isNotEmpty()) {
                        list.add(Triple(id, name, cover))
                    }
                }
                list
            }

            val plSemaphore = Semaphore(8)
            val playlists = coroutineScope {
                playlistSummaries.map { (plId, plName, plCover) ->
                    async {
                        plSemaphore.withPermit {
                            try {
                                val detailUrl = "${baseUrl(server)}rest/getPlaylist.view?id=$plId&${authQuery(server)}".toHttpUrl()
                                val detailReq = Request.Builder().url(detailUrl).build()
                                val (trackIds, newTracks) = client.newCall(detailReq).await().use { resp ->
                                    if (!resp.isSuccessful) return@use Pair(emptyList<String>(), emptyList<NetworkTrack>())
                                    val json = JSONObject(resp.body?.string() ?: "{}")
                                    val sub = json.optJSONObject("subsonic-response") ?: return@use Pair(emptyList(), emptyList())
                                    val plObj = sub.optJSONObject("playlist") ?: return@use Pair(emptyList(), emptyList())
                                    val entryArr = plObj.optJSONArray("entry") ?: return@use Pair(emptyList(), emptyList())
                                    val ids = mutableListOf<String>()
                                    val extraTracks = mutableListOf<NetworkTrack>()

                                    for (j in 0 until entryArr.length()) {
                                        val entry = entryArr.optJSONObject(j) ?: continue
                                        val songId = entry.optString("id")
                                        if (songId.isEmpty()) continue
                                        val trackUri = NetworkMediaUri.create(server.id, songId)
                                        ids.add(trackUri)

                                        val title = entry.optString("title", "Unknown Title")
                                        val artist = entry.optString("artist").takeIf { it.isNotBlank() }
                                        val album = entry.optString("album").takeIf { it.isNotBlank() }
                                        val cover = entry.optString("coverArt").takeIf { it.isNotBlank() }
                                        val duration = entry.optInt("duration", -1)
                                        val trackNum = entry.optInt("track", -1).takeIf { it > 0 }
                                        val year = entry.optInt("year", -1).takeIf { it > 0 }
                                        val size = entry.optLong("size", -1L)
                                        val artworkUrl = cover?.let {
                                            "${baseUrl(server)}rest/getCoverArt.view?id=$it&size=800&${authQuery(server)}"
                                        }

                                        extraTracks.add(
                                            NetworkTrack(
                                                id = trackUri,
                                                title = title,
                                                artist = artist,
                                                album = album,
                                                artwork = artworkUrl,
                                                size = size,
                                                duration = duration,
                                                year = year,
                                                trackNumber = trackNum,
                                                albumId = album?.let { "navidrome_album_${server.id}_${it.hashCode()}" },
                                                artistId = artist?.let { "navidrome_artist_${server.id}_${it.hashCode()}" },
                                            ),
                                        )
                                    }
                                    Pair(ids, extraTracks)
                                }

                                val artworkUrl = plCover?.let {
                                    "${baseUrl(server)}rest/getCoverArt.view?id=$it&size=800&${authQuery(server)}"
                                }

                                Pair(
                                    NetworkPlaylist(
                                        id = "navidrome_pl_${server.id}_$plId",
                                        name = plName,
                                        thumbnailUrl = artworkUrl,
                                        trackIds = trackIds,
                                    ),
                                    newTracks,
                                )
                            } catch (_: Exception) {
                                null
                            }
                        }
                    }
                }.awaitAll().filterNotNull()
            }

            for ((pl, extraTracks) in playlists) {
                result.add(pl)
                for (extra in extraTracks) {
                    if (existingTrackIds.add(extra.id)) {
                        tracks.add(extra)
                    }
                }
            }
        } catch (_: Exception) {}
        result
    }

    fun resolveStreamUrl(server: NetworkServer, songId: String): String {
        val cleanId = songId.removePrefix("/")
        return "${baseUrl(server)}rest/stream.view?id=$cleanId&maxBitRate=0&format=raw&${authQuery(server)}"
    }

    suspend fun fetchLyrics(
        server: NetworkServer,
        songId: String,
        artist: String? = null,
        title: String? = null,
    ): String? = withContext(Dispatchers.IO) {
        val cleanId = songId.removePrefix("/")

        // 1. Try OpenSubsonic getLyricsBySongId.view
        try {
            val url = "${baseUrl(server)}rest/getLyricsBySongId.view?id=$cleanId&${authQuery(server)}".toHttpUrl()
            val request = Request.Builder().url(url).build()
            val lyrics = client.newCall(request).await().use { response ->
                if (!response.isSuccessful) return@use null
                val json = JSONObject(response.body?.string() ?: "{}")
                val subResponse = json.optJSONObject("subsonic-response") ?: return@use null
                val lyricsList = subResponse.optJSONObject("lyricsList") ?: return@use null
                val structuredLyricsArray = lyricsList.optJSONArray("structuredLyrics") ?: return@use null
                if (structuredLyricsArray.length() == 0) return@use null

                var plainCandidate: String? = null
                for (i in 0 until structuredLyricsArray.length()) {
                    val structObj = structuredLyricsArray.optJSONObject(i) ?: continue
                    val isSynced = structObj.optBoolean("synced", false)
                    val lines = structObj.optJSONArray("line") ?: continue
                    val builder = StringBuilder()
                    for (l in 0 until lines.length()) {
                        val lineObj = lines.optJSONObject(l) ?: continue
                        val value = lineObj.optString("value", "")
                        if (isSynced) {
                            val startMs = lineObj.optLong("start", -1L)
                            if (startMs >= 0) {
                                val minutes = startMs / 60000
                                val seconds = (startMs % 60000) / 1000
                                val hundredths = (startMs % 1000) / 10
                                builder.append(String.format(Locale.US, "[%02d:%02d.%02d]%s\n", minutes, seconds, hundredths, value))
                            } else {
                                builder.append(value).append("\n")
                            }
                        } else {
                            builder.append(value).append("\n")
                        }
                    }
                    val formatted = builder.toString().trim()
                    if (formatted.isNotEmpty()) {
                        if (isSynced) return@use formatted
                        if (plainCandidate == null) plainCandidate = formatted
                    }
                }
                plainCandidate
            }
            if (!lyrics.isNullOrBlank()) return@withContext lyrics
        } catch (_: Exception) { }

        // 2. Fallback to standard Subsonic getLyrics.view
        if (!artist.isNullOrBlank() && !title.isNullOrBlank()) {
            try {
                val encArtist = java.net.URLEncoder.encode(artist, "UTF-8")
                val encTitle = java.net.URLEncoder.encode(title, "UTF-8")
                val url = "${baseUrl(server)}rest/getLyrics.view?artist=$encArtist&title=$encTitle&${authQuery(server)}".toHttpUrl()
                val request = Request.Builder().url(url).build()
                val lyrics = client.newCall(request).await().use { response ->
                    if (!response.isSuccessful) return@use null
                    val json = JSONObject(response.body?.string() ?: "{}")
                    val subResponse = json.optJSONObject("subsonic-response") ?: return@use null
                    val lyricsObj = subResponse.optJSONObject("lyrics") ?: return@use null
                    lyricsObj.optString("content").takeIf { it.isNotBlank() }
                }
                if (!lyrics.isNullOrBlank()) return@withContext lyrics
            } catch (_: Exception) { }
        }

        null
    }
}

private suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation { cancel() }
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            continuation.resumeWithException(StorageException(StorageError.CONNECTION, e))
        }
        override fun onResponse(call: Call, response: Response) {
            continuation.resume(response)
        }
    })
}
