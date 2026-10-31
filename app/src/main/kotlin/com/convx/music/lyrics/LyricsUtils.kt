package com.convx.music.lyrics

import android.text.format.DateUtils

@Suppress("RegExpRedundantEscape")
object LyricsUtils {
    val LINE_REGEX = "((\\[\\d\\d:\\d\\d\\.\\d{2,3}\\] ?)+)(.+)".toRegex()
    val TIME_REGEX = "\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})\\]".toRegex()

    // Regex for rich sync format: [MM:SS.mm]<MM:SS.mm> word <MM:SS.mm> word ...
    private val RICH_SYNC_LINE_REGEX = "\\[(\\d{1,2}):(\\d{2})\\.(\\d{2,3})\\](.+)".toRegex()
    private val RICH_SYNC_WORD_REGEX =
        Regex("<(\\d{1,2}):(\\d{2})\\.(\\d{2,3})(?:\\|(\\d{1,2}):(\\d{2})\\.(\\d{2,3}))?>\\s*([^<]+)")
    private val RICH_SYNC_TIMESTAMP_TAG_REGEX =
        Regex("<\\d{1,2}:\\d{2}\\.\\d{2,3}(?:\\|\\d{1,2}:\\d{2}\\.\\d{2,3})?>\\s*")

    // Regex for agent and background markers
    private val AGENT_REGEX = "\\{agent:([^}]+)\\}".toRegex()
    private val BACKGROUND_REGEX = "^\\{bg\\}".toRegex()

    private val HEX_ENTITY_REGEX = "&#x([0-9a-fA-F]+);".toRegex()
    private val DEC_ENTITY_REGEX = "&#(\\d+);".toRegex()

    private fun decodeHtmlEntities(text: String): String =
        text
            .replace(HEX_ENTITY_REGEX) { match ->
                match.groupValues[1].toIntOrNull(16)
                    ?.takeIf { it in 0..0x10FFFF }
                    ?.let { String(Character.toChars(it)) }
                    ?: match.value
            }
            .replace(DEC_ENTITY_REGEX) { match ->
                match.groupValues[1].toIntOrNull()
                    ?.takeIf { it in 0..0x10FFFF }
                    ?.let { String(Character.toChars(it)) }
                    ?: match.value
            }
            .replace("&apos;", "'")
            .replace("&quot;", "\"")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")

    fun parseLyrics(lyrics: String): List<LyricsEntry> {
        // Unescape JSON string if needed
        val unescapedLyrics = lyrics
            .trim()
            .removePrefix("\"")
            .removeSuffix("\"")
            .replace("\\\\", "\\")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")

        // Decode HTML entities (e.g. &#x27; -> ', &amp; -> &)
        val decodedLyrics = decodeHtmlEntities(unescapedLyrics)

        val lines = decodedLyrics.lineSequence()
            .filter { it.isNotBlank() && !it.trim().startsWith("[offset:") }
            .toList()

        val isRichSync = lines.any { line ->
            RICH_SYNC_LINE_REGEX.matches(line.trim()) &&
            RICH_SYNC_WORD_REGEX.containsMatchIn(line)
        }

        return if (isRichSync) {
            parseRichSyncLyrics(lines)
        } else {
            parseStandardLyrics(lines)
        }
    }

    /**
     * Parse rich sync lyrics format: [MM:SS.mm]<MM:SS.mm> word <MM:SS.mm> word ...
     * This format provides word-by-word timing for karaoke-style highlighting
     */
    private fun parseRichSyncLyrics(lines: List<String>): List<LyricsEntry> {
        val result = mutableListOf<LyricsEntry>()

        lines.forEachIndexed { index, line ->
            val matchResult = RICH_SYNC_LINE_REGEX.matchEntire(line.trim())
                ?: return@forEachIndexed

            val minutes = matchResult.groupValues[1].toLongOrNull() ?: 0L
            val seconds = matchResult.groupValues[2].toLongOrNull() ?: 0L
            val fractionText = matchResult.groupValues[3]
            val fraction = fractionText.toLongOrNull() ?: 0L
            val fractionMillis = if (fractionText.length == 3) fraction else fraction * 10
            val lineTimeMs =
                minutes * DateUtils.MINUTE_IN_MILLIS +
                    seconds * DateUtils.SECOND_IN_MILLIS +
                    fractionMillis

            var content = matchResult.groupValues[4].trimStart()

            // Keep the vocalist marker as metadata rather than lyric text.
            val agentMatch = AGENT_REGEX.find(content)
            val agent = agentMatch?.groupValues?.get(1)
            if (agentMatch != null) {
                content = content.replaceFirst(AGENT_REGEX, "")
            }

            val isBackground = BACKGROUND_REGEX.containsMatchIn(content)
            if (isBackground) {
                content = content.replaceFirst(BACKGROUND_REGEX, "")
            }

            val wordTimings = parseRichSyncWords(content, index, lines)
            val plainText = content.replace(RICH_SYNC_TIMESTAMP_TAG_REGEX, "").trim()
            if (plainText.isBlank()) return@forEachIndexed

            result.add(
                LyricsEntry(
                    time = lineTimeMs,
                    text = plainText,
                    words = wordTimings,
                    agent = agent,
                    isBackground = isBackground,
                ),
            )
        }

        return result.sorted()
    }

    /**
     * Parse word timestamps from rich sync content
     * Format: <MM:SS.mm> word <MM:SS.mm> word ...
     */
    private fun parseRichSyncWords(content: String, currentIndex: Int, allLines: List<String>): List<WordTimestamp>? {
        val wordMatches = RICH_SYNC_WORD_REGEX.findAll(content).toList()

        if (wordMatches.isEmpty()) return null

        val wordTimings = mutableListOf<WordTimestamp>()

        wordMatches.forEachIndexed { index, match ->
            val startTimeSeconds = parseRichSyncTime(match)

            val wordText = match.groupValues[7].trim()
            val declaredEnd = if (match.groupValues[4].isNotEmpty()) parseRichSyncTime(match, 4) else null

            // Calculate end time: use next word's start time, or estimate from next line
            val nextWordStart = if (index < wordMatches.size - 1) {
                parseRichSyncTime(wordMatches[index + 1])
            } else {
                val nextLineTime = getNextLineStartTime(currentIndex, allLines)
                nextLineTime
            }
            val endTimeSeconds = when {
                declaredEnd != null && nextWordStart != null -> minOf(declaredEnd, nextWordStart)
                declaredEnd != null -> declaredEnd
                nextWordStart != null -> nextWordStart
                else -> startTimeSeconds + 0.5
            }.coerceAtLeast(startTimeSeconds + 0.02)

            if (wordText.isNotBlank()) {
                wordTimings.add(WordTimestamp(wordText, startTimeSeconds, endTimeSeconds))
            }
        }

        return if (wordTimings.isNotEmpty()) wordTimings else null
    }

    private fun parseRichSyncTime(match: MatchResult, groupOffset: Int = 1): Double {
        val minutes = match.groupValues[groupOffset].toLongOrNull() ?: 0L
        val seconds = match.groupValues[groupOffset + 1].toLongOrNull() ?: 0L
        val fractionText = match.groupValues[groupOffset + 2]
        val fraction = fractionText.toLongOrNull() ?: 0L
        val fractionScale = if (fractionText.length == 3) 1000.0 else 100.0

        return minutes * 60.0 + seconds + fraction / fractionScale
    }

    /**
     * Get the start time of the next line for calculating the last word's end time
     */
    private fun getNextLineStartTime(currentIndex: Int, allLines: List<String>): Double? {
        if (currentIndex + 1 >= allLines.size) return null

        val nextLine = allLines[currentIndex + 1].trim()
        val matchResult = RICH_SYNC_LINE_REGEX.matchEntire(nextLine) ?: return null

        return parseRichSyncTime(matchResult)
    }

    /**
     * Parse standard synced lyrics format: [MM:SS.mm] text
     */
    private fun parseStandardLyrics(lines: List<String>): List<LyricsEntry> {
        val result = mutableListOf<LyricsEntry>()

        for (index in lines.indices) {
            val line = lines[index]
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("<") && trimmedLine.endsWith(">")) continue

            val entries = parseLine(line) ?: continue
            val nextLine = lines.getOrNull(index + 1)?.trim()
            val wordTimestamps = nextLine
                ?.takeIf { it.startsWith("<") && it.endsWith(">") }
                ?.let { parseWordTimestamps(it.removeSurrounding("<", ">")) }

            if (wordTimestamps == null) {
                result.addAll(entries)
            } else {
                result.addAll(
                    entries.map { entry ->
                        LyricsEntry(
                            time = entry.time,
                            text = entry.text,
                            words = wordTimestamps,
                            agent = entry.agent,
                            isBackground = entry.isBackground,
                        )
                    },
                )
            }
        }
        return result.sorted()
    }

    private fun parseWordTimestamps(data: String): List<WordTimestamp>? {
        if (data.isBlank()) return null
        return try {
            data.split("|").mapNotNull { wordData ->
                val parts = wordData.split(":")
                if (parts.size == 3) {
                    WordTimestamp(
                        text = parts[0],
                        startTime = parts[1].toDouble(),
                        endTime = parts[2].toDouble()
                    )
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseLine(line: String, words: List<WordTimestamp>? = null): List<LyricsEntry>? {
        if (line.isEmpty()) {
            return null
        }
        val matchResult = LINE_REGEX.matchEntire(line.trim()) ?: return null
        val times = matchResult.groupValues[1]
        var text = matchResult.groupValues[3]
        val timeMatchResults = TIME_REGEX.findAll(times)

        // Parse agent marker {agent:v1}
        val agentMatch = AGENT_REGEX.find(text)
        val agent = agentMatch?.groupValues?.get(1)
        if (agentMatch != null) {
            text = text.replaceFirst(AGENT_REGEX, "")
        }

        // Parse background marker {bg}
        val isBackground = BACKGROUND_REGEX.containsMatchIn(text)
        if (isBackground) {
            text = text.replaceFirst(BACKGROUND_REGEX, "")
        }

        return timeMatchResults
            .map { timeMatchResult ->
                val min = timeMatchResult.groupValues[1].toLong()
                val sec = timeMatchResult.groupValues[2].toLong()
                val milString = timeMatchResult.groupValues[3]
                var mil = milString.toLong()
                if (milString.length == 2) {
                    mil *= 10
                }
                val time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil
                LyricsEntry(time, text, words, agent = agent, isBackground = isBackground)
            }.toList()
    }

    fun findCurrentLineIndex(
        lines: List<LyricsEntry>,
        position: Long,
    ): Int {
        var low = 0
        var high = lines.size - 1
        var firstPast = lines.size
        val target = position + 300L
        while (low <= high) {
            val mid = (low + high) ushr 1
            if (lines[mid].time >= target) {
                firstPast = mid
                high = mid - 1
            } else {
                low = mid + 1
            }
        }
        return if (firstPast == 0) -1 else firstPast - 1
    }
}
