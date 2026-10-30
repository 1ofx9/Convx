package com.convx.music.playback.smart

import android.content.Context
import android.net.Uri
import androidx.media3.common.util.UnstableApi
import java.io.File

object StemStore {
    private fun directory(context: Context) = File(context.filesDir, "stems")

    private fun base(id: String) = "${id.hashCode().toUInt()}_${id.length}"

    fun stem(context: Context, id: String, name: String) = File(directory(context), "${base(id)}_$name.m4a")

    fun vocals(context: Context, id: String) = stem(context, id, "vocals")

    fun instrumental(context: Context, id: String) = stem(context, id, "instrumental")

    fun has(context: Context, id: String) = vocals(context, id).exists()

    fun totalBytes(context: Context): Long = directory(context).listFiles()?.sumOf { it.length() } ?: 0L

    fun clear(context: Context) {
        directory(context).deleteRecursively()
    }

    fun estimatedBytes(durationSeconds: Int, stems: Int = 2): Long = durationSeconds * stems * 96_000L / 8L

    internal fun prepare(context: Context) = directory(context).apply { mkdirs() }
}

@UnstableApi
object StemSeparator {
    private const val TAG = "ConvxStemSeparator"

    const val MAX_SECONDS = 12 * 60

    fun separate(
        context: Context,
        id: String,
        uri: Uri,
        durationSeconds: Int,
        quality: StemQuality,
        isCurrent: () -> Boolean,
    ): Boolean = false
}
