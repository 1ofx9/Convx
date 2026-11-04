/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.screens.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.convx.music.db.MusicDatabase
import com.convx.music.db.entities.Album
import com.convx.music.db.entities.Artist
import com.convx.music.db.entities.Song
import com.convx.music.sync.NetworkSyncManager
import com.convx.music.utils.LocalAudioScanner
import com.convx.music.utils.LocalFolderIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LocalMusicViewModel @Inject constructor(
    private val database: MusicDatabase,
    val syncManager: NetworkSyncManager,
) : ViewModel() {

    val songs: StateFlow<List<Song>> = database.localSongsByNameAsc()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<Album>> = database.albumsLocalByNameAsc()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<Artist>> = database.artistsLocalByNameAsc()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<LocalFolderIndex.Folder>> = MutableStateFlow(emptyList())

    val isScanning: StateFlow<Boolean> = syncManager.syncState
        .map { it.isSyncing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _scanResult = MutableStateFlow<LocalAudioScanner.ScanResult?>(null)
    val scanResult: StateFlow<LocalAudioScanner.ScanResult?> = _scanResult.asStateFlow()

    fun scanDevice(context: Context? = null, force: Boolean = true) {
        syncManager.sync(force = force)
    }
}
