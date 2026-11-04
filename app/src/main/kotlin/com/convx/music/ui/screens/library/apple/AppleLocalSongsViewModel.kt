/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.screens.library.apple

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.convx.music.constants.SongSortType
import com.convx.music.db.MusicDatabase
import com.convx.music.db.entities.Song
import com.convx.music.sync.NetworkSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Songs synced from Navidrome/Subsonic server for Library's Songs page. */
@HiltViewModel
class AppleLocalSongsViewModel @Inject constructor(
    private val database: MusicDatabase,
    val syncManager: NetworkSyncManager,
) : ViewModel() {
    private val order = MutableStateFlow(SongSortType.NAME to false)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val songs: StateFlow<List<Song>> = order
        .flatMapLatest { (type, desc) -> database.localSongs(type, desc) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val scanning: StateFlow<Boolean> = syncManager.syncState
        .map { it.isSyncing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun sort(type: SongSortType, descending: Boolean) {
        order.value = type to descending
    }

    /** Synchronizes songs with configured network servers. */
    fun scan(context: Context? = null) {
        syncManager.sync(force = true)
    }
}
