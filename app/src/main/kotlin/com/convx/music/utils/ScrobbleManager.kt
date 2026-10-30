/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.utils

import com.convx.music.models.MediaMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

class ScrobbleManager(
    private val scope: CoroutineScope,
    var minSongDuration: Int = 30,
    var scrobbleDelayPercent: Float = 0.5f,
    var scrobbleDelaySeconds: Int = 50
) {
    private var scrobbleJob: Job? = null
    var useNowPlaying = true

    fun destroy() {
        scrobbleJob?.cancel()
    }

    fun onSongStart(metadata: MediaMetadata?, duration: Long? = null) {}

    fun onSongResume(metadata: MediaMetadata) {}

    fun onSongPause() {}

    fun onSongStop() {}

    fun onPlayerStateChanged(isPlaying: Boolean, metadata: MediaMetadata?, duration: Long? = null) {}
}
