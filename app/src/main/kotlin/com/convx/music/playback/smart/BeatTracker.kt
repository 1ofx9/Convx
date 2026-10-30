package com.convx.music.playback.smart

import android.content.Context

/** Optional Beat This! model stub (ONNX runtime removed). */
class BeatTracker(private val context: Context) {
    data class Grid(
        val beats: List<Double>,
        val downbeats: List<Double>,
        val bpm: Double,
        val beatInterval: Double,
        val firstBeat: Double,
        val beatConfidence: Double,
    )

    fun track(pcm: FloatArray, offsetSeconds: Double = 0.0): Grid? = null

    fun release() {}

    companion object {
        const val WINDOW_SECONDS: Double = 30.0
        fun gridConfidence(beats: List<Double>, peakLogits: List<Double>): Double = 0.0
    }
}
