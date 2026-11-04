/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.utils

import androidx.room.withTransaction
import com.convx.music.db.MusicDatabase
import com.convx.music.db.entities.AlbumEntity
import com.convx.music.db.entities.ArtistEntity
import com.convx.music.db.entities.PlaylistEntity
import com.convx.music.db.entities.PlaylistSongMap
import com.convx.music.db.entities.SongAlbumMap
import com.convx.music.db.entities.SongArtistMap
import com.convx.music.db.entities.SongEntity
import com.convx.storage.domain.NetworkLibraryRepository
import com.convx.storage.domain.NetworkTrack
import java.time.LocalDateTime
import timber.log.Timber

/**
 * Puts the audio found on an SMB, WebDAV, or Navidrome source into the library as local songs.
 * A song's id is its `convx-network://` URI, which the player routes back through the
 * network data source, streaming lossless audio directly without copying to the phone.
 */
object NetworkLibraryImporter {

    data class Result(val added: Int, val skipped: Int, val truncated: Boolean)

    suspend fun import(
        database: MusicDatabase,
        repository: NetworkLibraryRepository,
        serverId: String,
    ): Result {
        val library = repository.scan(serverId)
        if (library.tracks.isEmpty() && library.playlists.isEmpty()) {
            return Result(0, 0, library.truncated)
        }

        val now = LocalDateTime.now()
        val songsToUpsert = ArrayList<SongEntity>(library.tracks.size)
        val artistsToInsert = LinkedHashMap<String, ArtistEntity>()
        val albumsToInsert = LinkedHashMap<String, AlbumEntity>()
        val songArtistMaps = ArrayList<SongArtistMap>(library.tracks.size)
        val songAlbumMaps = ArrayList<SongAlbumMap>(library.tracks.size)

        for (track in library.tracks) {
            val artistName = track.artist?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
            val albumName = track.album.orEmpty()
            val artistId = track.artistId?.takeIf { it.isNotBlank() } ?: "network_artist_${artistName.hashCode()}"
            val albumId = track.albumId?.takeIf { it.isNotBlank() } ?: "network_album_${albumName.hashCode()}"
            val duration = if (track.duration > 0) track.duration else -1

            songsToUpsert.add(
                SongEntity(
                    id = track.id,
                    title = track.title,
                    duration = duration,
                    thumbnailUrl = track.artwork,
                    albumId = albumId.takeIf { albumName.isNotBlank() },
                    albumName = albumName.takeIf { it.isNotBlank() },
                    year = track.year,
                    isLocal = true,
                    inLibrary = now,
                ),
            )

            artistsToInsert.putIfAbsent(artistId, ArtistEntity(id = artistId, name = artistName, isLocal = true))
            songArtistMaps.add(SongArtistMap(songId = track.id, artistId = artistId, position = 0))

            if (albumName.isNotBlank()) {
                albumsToInsert.putIfAbsent(
                    albumId,
                    AlbumEntity(
                        id = albumId,
                        title = albumName,
                        thumbnailUrl = track.artwork,
                        year = track.year,
                        songCount = 0,
                        duration = 0,
                        isLocal = true,
                        inLibrary = now,
                    ),
                )
                songAlbumMaps.add(SongAlbumMap(songId = track.id, albumId = albumId, index = track.trackNumber ?: 0))
            }
        }

        try {
            database.withTransaction {
                songsToUpsert.chunked(500).forEach { database.upsertSongs(it) }
                artistsToInsert.values.chunked(500).forEach { database.insertArtists(it) }
                albumsToInsert.values.chunked(500).forEach { database.insertAlbums(it) }
                songArtistMaps.chunked(500).forEach { database.insertSongArtistMaps(it) }
                songAlbumMaps.chunked(500).forEach { database.insertSongAlbumMaps(it) }

                library.playlists.forEach { pl ->
                    try {
                        val entity = PlaylistEntity(
                            id = pl.id,
                            name = pl.name,
                            thumbnailUrl = pl.thumbnailUrl,
                            isLocal = true,
                            bookmarkedAt = now,
                            lastUpdateTime = now,
                            createdAt = now,
                            isEditable = true,
                        )
                        database.insert(entity)
                        database.update(entity)
                        database.clearPlaylist(pl.id)
                        val plMaps = pl.trackIds.mapIndexed { index, trackId ->
                            PlaylistSongMap(
                                playlistId = pl.id,
                                songId = trackId,
                                position = index,
                            )
                        }
                        plMaps.chunked(500).forEach { database.insertPlaylistSongMaps(it) }
                    } catch (e: Exception) {
                        Timber.tag("NetworkImport").w(e, "Failed to import playlist ${pl.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.tag("NetworkImport").e(e, "Failed to batch insert library")
        }

        return Result(library.tracks.size, 0, library.truncated)
    }

    suspend fun insert(database: MusicDatabase, track: NetworkTrack): Boolean {
        if (database.getSongById(track.id) != null) return false
        val artistName = track.artist?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
        val albumName = track.album.orEmpty()
        val artistId = track.artistId?.takeIf { it.isNotBlank() } ?: "network_artist_${artistName.hashCode()}"
        val albumId = track.albumId?.takeIf { it.isNotBlank() } ?: "network_album_${albumName.hashCode()}"
        val now = LocalDateTime.now()
        val duration = if (track.duration > 0) track.duration else -1
        return try {
            database.withTransaction {
                upsert(
                    SongEntity(
                        id = track.id,
                        title = track.title,
                        duration = duration,
                        thumbnailUrl = track.artwork,
                        albumId = albumId.takeIf { albumName.isNotBlank() },
                        albumName = albumName.takeIf { it.isNotBlank() },
                        year = track.year,
                        isLocal = true,
                        inLibrary = now,
                    ),
                )
                insert(ArtistEntity(id = artistId, name = artistName, isLocal = true))
                insert(SongArtistMap(songId = track.id, artistId = artistId, position = 0))
                if (albumName.isNotBlank()) {
                    insert(
                        AlbumEntity(
                            id = albumId,
                            title = albumName,
                            thumbnailUrl = track.artwork,
                            year = track.year,
                            songCount = 0,
                            duration = 0,
                            isLocal = true,
                            inLibrary = now,
                        ),
                    )
                    insert(SongAlbumMap(songId = track.id, albumId = albumId, index = track.trackNumber ?: 0))
                }
            }
            true
        } catch (e: Exception) {
            Timber.tag("NetworkImport").w(e, "Failed to insert ${track.title}")
            false
        }
    }
}
