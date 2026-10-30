package com.convx.storage.data

import androidx.media3.datasource.DataSource
import com.convx.storage.domain.*
import com.convx.storage.playback.NetworkDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.coroutines.coroutineContext

class NetworkMusicRepository(
    private val store: NetworkServerStore,
    private val webDav: WebDavFileSource = WebDavFileSource(),
    private val smb: SmbFileSource = SmbFileSource(),
    private val navidrome: NavidromeFileSource = NavidromeFileSource(),
) : NetworkLibraryRepository {
    override val servers = store.servers
    override suspend fun loadServers() = store.load()
    override suspend fun save(server: NetworkServer) {
        store.save(server)
        smb.invalidate(server.id)
    }
    override suspend fun delete(id: String) {
        store.delete(id)
        smb.invalidate(id)
    }

    override suspend fun test(server: NetworkServer) {
        when (server) {
            is NetworkServer.WebDav -> webDav.list(server, "")
            is NetworkServer.Smb -> smb.list(server, "")
            is NetworkServer.Navidrome -> navidrome.test(server)
        }
    }

    override suspend fun browse(id: String, folder: String): List<NetworkEntry> {
        val server = store.find(id) ?: throw StorageException(StorageError.NOT_FOUND)
        return when (server) {
            is NetworkServer.WebDav -> webDav.list(server, RemotePath.normalize(folder))
            is NetworkServer.Smb -> smb.list(server, RemotePath.normalize(folder))
            is NetworkServer.Navidrome -> emptyList()
        }
    }

    override suspend fun scan(id: String, maxTracks: Int): NetworkLibrary = withContext(Dispatchers.IO) {
        require(maxTracks in 1..100_000)
        val server = store.find(id) ?: throw StorageException(StorageError.NOT_FOUND)
        when (server) {
            is NetworkServer.Navidrome -> navidrome.scan(server, maxTracks)
            is NetworkServer.WebDav, is NetworkServer.Smb -> {
                val files = LinkedHashMap<String, NetworkEntry>()
                val images = LinkedHashMap<String, NetworkEntry>()
                val visited = HashSet<String>()
                val folders = ArrayDeque<String>().apply { add("") }
                var truncated = false
                val source = if (server is NetworkServer.WebDav) webDav else smb
                while (folders.isNotEmpty()) {
                    coroutineContext.ensureActive()
                    val folder = folders.removeFirst()
                    if (!visited.add(folder)) continue
                    if (visited.size > 10_000) { truncated = true; break }
                    for (entry in source.list(server, folder)) {
                        coroutineContext.ensureActive()
                        when {
                            entry.directory -> if (entry.path !in visited) folders.add(entry.path)
                            NetworkMedia.isAudio(entry.name) -> {
                                if (files.size >= maxTracks) { truncated = true; break }
                                files[entry.path] = entry
                            }
                            NetworkMedia.isImage(entry.name) -> if (images.size < 10_000) images[entry.path] = entry
                        }
                    }
                    if (truncated) break
                }
                val byFolder = images.values.groupBy { RemotePath.parent(it.path) }
                NetworkLibrary(files.values.map { NetworkMedia.track(id, it, byFolder[RemotePath.parent(it.path)].orEmpty()) }, truncated)
            }
        }
    }

    override suspend fun readArtwork(uri: String, maxBytes: Int): ByteArray = withContext(Dispatchers.IO) {
        require(maxBytes in 1..16 * 1024 * 1024)
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            val req = okhttp3.Request.Builder().url(uri).build()
            return@withContext navidrome.client.newCall(req).execute().use { res ->
                val body = res.body ?: throw StorageException(StorageError.NOT_FOUND)
                val bytes = body.bytes()
                if (bytes.size > maxBytes) throw StorageException(StorageError.LIMIT_REACHED)
                bytes
            }
        }
        val location = NetworkMediaUri.parse(uri) ?: throw StorageException(StorageError.INVALID_CONFIGURATION)
        val server = store.find(location.serverId) ?: throw StorageException(StorageError.NOT_FOUND)
        val source: NetworkFileSource = when (server) {
            is NetworkServer.WebDav -> webDav
            is NetworkServer.Smb -> smb
            else -> throw StorageException(StorageError.INVALID_CONFIGURATION)
        }
        source.open(server, location.path).use { file ->
            if (file.length > maxBytes) throw StorageException(StorageError.LIMIT_REACHED)
            val bytes = ByteArrayOutputStream()
            val buffer = ByteArray(64 * 1024)
            var position = 0L
            while (true) {
                coroutineContext.ensureActive()
                val count = file.read(position, buffer, 0, minOf(buffer.size.toLong(), maxBytes - position + 1).toInt())
                if (count < 0 || count == 0) break
                position += count
                if (position > maxBytes) throw StorageException(StorageError.LIMIT_REACHED)
                bytes.write(buffer, 0, count)
            }
            bytes.toByteArray()
        }
    }

    override suspend fun fetchLyrics(uri: String, artist: String?, title: String?): String? = withContext(Dispatchers.IO) {
        val location = NetworkMediaUri.parse(uri) ?: return@withContext null
        val server = store.find(location.serverId) ?: return@withContext null
        when (server) {
            is NetworkServer.Navidrome -> navidrome.fetchLyrics(server, location.path, artist, title)
            else -> null
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun dataSourceFactory(downstream: DataSource.Factory): DataSource.Factory =
        NetworkDataSourceFactory(downstream, store, webDav, smb, navidrome)

    private fun source(server: NetworkServer): NetworkFileSource = when (server) {
        is NetworkServer.WebDav -> webDav
        is NetworkServer.Smb -> smb
        else -> throw StorageException(StorageError.INVALID_CONFIGURATION)
    }
}
