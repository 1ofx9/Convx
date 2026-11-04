/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.sync

import com.convx.music.db.MusicDatabase
import com.convx.music.di.ApplicationScope
import com.convx.music.utils.NetworkLibraryImporter
import com.convx.storage.data.NetworkMusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkSyncManager @Inject constructor(
    private val database: MusicDatabase,
    private val repository: NetworkMusicRepository,
    @ApplicationScope private val scope: CoroutineScope,
) {
    data class SyncState(
        val isSyncing: Boolean = false,
        val message: String? = null,
        val error: String? = null,
        val lastSyncedAt: Long = 0L,
        val totalAdded: Int = 0,
    )

    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    @Volatile
    private var activeJob: Job? = null

    fun sync(force: Boolean = false): Job {
        val runningJob = activeJob
        if (runningJob != null && runningJob.isActive) {
            return runningJob
        }

        val now = System.currentTimeMillis()
        if (!force && (now - _syncState.value.lastSyncedAt < 20_000L)) {
            return scope.launch { }
        }

        val job = scope.launch {
            _syncState.value = _syncState.value.copy(
                isSyncing = true,
                message = "Connecting to server…",
                error = null,
            )
            try {
                repository.loadServers()
                val servers = repository.servers.value
                if (servers.isEmpty()) {
                    _syncState.value = _syncState.value.copy(
                        isSyncing = false,
                        message = "No servers configured",
                    )
                    return@launch
                }

                var totalAdded = 0
                servers.forEach { server ->
                    _syncState.value = _syncState.value.copy(
                        message = "Syncing ${server.name}…",
                    )
                    val result = NetworkLibraryImporter.import(database, repository, server.id)
                    totalAdded += result.added
                }

                _syncState.value = SyncState(
                    isSyncing = false,
                    message = "Synced $totalAdded tracks",
                    lastSyncedAt = System.currentTimeMillis(),
                    totalAdded = totalAdded,
                )
            } catch (e: Exception) {
                Timber.tag("NetworkSyncManager").e(e, "Sync failed")
                _syncState.value = _syncState.value.copy(
                    isSyncing = false,
                    error = e.message ?: "Sync failed",
                    message = null,
                )
            } finally {
                activeJob = null
            }
        }
        activeJob = job
        return job
    }
}
