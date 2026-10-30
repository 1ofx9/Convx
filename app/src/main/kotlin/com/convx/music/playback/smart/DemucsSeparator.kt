package com.convx.music.playback.smart

import android.content.Context
import java.io.File

enum class StemQuality { FAST, DEMUCS }

enum class DemucsInstall { OK, DOWNLOAD_FAILED, INCOMPATIBLE }

/** Optional Demucs model stub (stem separation removed). */
object DemucsModel {
    fun isInstalled(context: Context): Boolean = false
    fun sizeBytes(context: Context): Long = 0L
    fun delete(context: Context) {}
    internal fun path(context: Context): String = ""
    suspend fun install(context: Context, url: String, onProgress: (Float) -> Unit): DemucsInstall =
        DemucsInstall.DOWNLOAD_FAILED
}

class DemucsSeparator(private val context: Context) {
    fun separate(
        audioPath: String,
        outputDir: File,
        onProgress: (Float) -> Unit = {}
    ): Boolean = false
}
