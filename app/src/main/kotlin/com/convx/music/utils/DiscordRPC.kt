package com.convx.music.utils

import android.content.Context
import com.convx.music.db.entities.Song

/**
 * DiscordRPC stub (Discord integration removed).
 */
class DiscordRPC(val context: Context, val token: String) {
    fun isRpcRunning(): Boolean = false
    fun closeRPC() {}
    fun close() {}
    suspend fun updateSong(
        song: Song,
        currentPlaybackTimeMillis: Long,
        playbackSpeed: Float = 1.0f,
        useDetails: Boolean = false,
        status: String = "online",
        button1Text: String = "",
        button1Visible: Boolean = true,
        button2Text: String = "",
        button2Visible: Boolean = true,
        activityType: String = "listening",
        activityName: String = "",
    ): Result<Unit> = Result.success(Unit)
}
