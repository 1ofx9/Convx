package com.convx.music.playback.smart

import android.content.Context

object VocalSpectrogram {
    val available: Boolean get() = false
    val sampleRate: Double = 44_100.0
    val hop: Int = 1024
    val frameRate: Double get() = sampleRate / hop
}

/** Optional vocal tracker stub (ONNX runtime removed). */
class VocalTracker(private val context: Context) {
    fun track(left: FloatArray, right: FloatArray, rate: Double): FloatArray? = null

    fun separateVocals(
        left: FloatArray,
        right: FloatArray,
        isCurrent: () -> Boolean,
        sink: (FloatArray, FloatArray, Int, Int) -> Unit,
    ): Boolean = false

    fun release() {}

    companion object {
        const val FIXED_FRAMES = 960
    }
}
