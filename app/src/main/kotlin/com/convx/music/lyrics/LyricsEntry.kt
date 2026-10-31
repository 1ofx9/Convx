package com.convx.music.lyrics

data class WordTimestamp(
    val text: String,
    val startTime: Double,
    val endTime: Double
)

data class LyricsEntry(
    val time: Long,
    val text: String,
    val words: List<WordTimestamp>? = null,
    val agent: String? = null,
    val isBackground: Boolean = false
) : Comparable<LyricsEntry> {
    override operator fun compareTo(other: LyricsEntry): Int = (time - other.time).toInt()

    companion object {
        val HEAD_LYRICS_ENTRY = LyricsEntry(0L, "")
    }
}
