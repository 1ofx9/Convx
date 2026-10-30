package com.convx.storage.domain

import java.net.URI
import java.util.Locale

object RemotePath {
    fun normalize(value: String): String {
        require('\u0000' !in value) { "Invalid folder path" }
        val parts = value.replace('\\', '/').split('/').filter(String::isNotEmpty)
        require(parts.none { it == ".." }) { "Folder path escapes the source" }
        return parts.filterNot { it == "." }.joinToString("/")
    }

    fun parent(path: String): String = normalize(path).substringBeforeLast('/', "")
    fun name(path: String): String = normalize(path).substringAfterLast('/')
    fun child(folder: String, name: String): String {
        require(name.isNotBlank() && '/' !in name && '\\' !in name && name != "." && name != "..") { "Invalid file name" }
        return normalize(listOf(folder, name).filter(String::isNotEmpty).joinToString("/"))
    }
}

data class NetworkMediaLocation(val serverId: String, val path: String)

object NetworkMediaUri {
    const val Scheme = "convx-network"

    fun create(serverId: String, path: String): String {
        require(serverId.matches(Regex("[A-Za-z0-9_-]{1,80}"))) { "Invalid server identifier" }
        return URI(Scheme, serverId, "/" + RemotePath.normalize(path), null, null).toASCIIString()
    }

    fun parse(value: String): NetworkMediaLocation? = try {
        val uri = URI(value)
        if (uri.scheme != Scheme || uri.rawAuthority.isNullOrBlank() || uri.query != null || uri.fragment != null || uri.userInfo != null) null
        else NetworkMediaLocation(uri.rawAuthority, RemotePath.normalize(uri.path.orEmpty()))
    } catch (_: Exception) { null }
}

data class NetworkEntry(val path: String, val name: String, val directory: Boolean, val size: Long = -1)

data class NetworkPlaylist(
    val id: String,
    val name: String,
    val thumbnailUrl: String? = null,
    val trackIds: List<String> = emptyList(),
)

data class NetworkLibrary(
    val tracks: List<NetworkTrack>,
    val truncated: Boolean = false,
    val playlists: List<NetworkPlaylist> = emptyList(),
)

data class NetworkTrack(
    val id: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val artwork: String?,
    val size: Long,
    val duration: Int = -1,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val albumId: String? = null,
    val artistId: String? = null,
)

object NetworkMedia {
    private val audioExtensions = setOf("mp3", "m4a", "flac", "ogg", "opus", "aac", "wav", "wma", "aif", "aiff", "webm")
    private val imageExtensions = setOf("jpg", "jpeg", "png", "webp")
    fun isAudio(name: String): Boolean = name.substringAfterLast('.', "").lowercase(Locale.ROOT) in audioExtensions
    fun isImage(name: String): Boolean = name.substringAfterLast('.', "").lowercase(Locale.ROOT) in imageExtensions

    fun track(serverId: String, file: NetworkEntry, siblings: List<NetworkEntry>): NetworkTrack {
        val artwork = siblings.firstOrNull { isImage(it.name) }?.let { NetworkMediaUri.create(serverId, it.path) }
        return NetworkTrack(
            id = NetworkMediaUri.create(serverId, file.path),
            title = RemotePath.name(file.path).substringBeforeLast('.'),
            artist = null,
            album = RemotePath.parent(file.path).substringAfterLast('/').takeIf { it.isNotBlank() },
            artwork = artwork,
            size = file.size,
        )
    }
}
