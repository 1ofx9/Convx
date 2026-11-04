/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.convx.music.constants.LocalAlbumsByYearKey
import com.convx.music.constants.LocalSongSortDescendingKey
import com.convx.music.constants.LocalSongSortTypeKey
import com.convx.music.constants.PlaylistSortType
import com.convx.music.constants.SongSortType
import com.convx.music.db.MusicDatabase
import com.convx.music.db.entities.Album
import com.convx.music.db.entities.Artist
import com.convx.music.db.entities.Playlist
import com.convx.music.db.entities.Song
import com.convx.music.extensions.toEnum
import com.convx.music.sync.NetworkSyncManager
import com.convx.music.utils.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Home in local-only mode: Room rows observed reactively, synced from network servers. */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class LocalHomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MusicDatabase,
    val syncManager: NetworkSyncManager,
) : ViewModel() {
    val songs: StateFlow<List<Song>> = context.dataStore.data
        .map { it[LocalSongSortTypeKey].toEnum(SongSortType.NAME) to (it[LocalSongSortDescendingKey] ?: false) }
        .distinctUntilChanged()
        .flatMapLatest { (sortType, descending) -> database.localSongs(sortType, descending) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Newest release first by default; SQLite sorts files without a year last on DESC.
    val albums: StateFlow<List<Album>> = context.dataStore.data
        .map { it[LocalAlbumsByYearKey] ?: true }
        .distinctUntilChanged()
        .flatMapLatest { byYear -> if (byYear) database.albumsLocalByYearDesc() else database.albumsLocalByNameAsc() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<Artist>> = database.artistsLocalByNameAsc()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Playlists synced from Navidrome
    val playlists: StateFlow<List<Playlist>> = database.playlists(PlaylistSortType.CREATE_DATE, true)
        .map { list -> list.filter { it.playlist.browseId == null } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncState: StateFlow<NetworkSyncManager.SyncState> = syncManager.syncState

    /** Synchronizes with configured Navidrome/Subsonic servers. */
    fun refresh() {
        syncManager.sync(force = true)
    }
}
