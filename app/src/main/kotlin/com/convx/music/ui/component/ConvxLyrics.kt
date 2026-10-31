package com.convx.music.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import com.convx.music.ui.ios.compat.AppTheme
import com.convx.music.ui.ios.compat.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.convx.music.constants.AppleMusicLyricsBlurKey
import com.convx.music.lyrics.LyricsEntry
import com.convx.music.lyrics.wordsWithSpacing
import com.convx.music.ui.screens.settings.LyricsPosition
import com.convx.music.utils.rememberPreference

/**
 * Synced lyrics line rendering.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConvxLyricsLine(
    fontFamily: androidx.compose.ui.text.font.FontFamily = androidx.compose.ui.text.font.FontFamily.Default,
    entry: LyricsEntry,
    nextEntryTime: Long?,
    effectivePlaybackPosition: Long,
    isSynced: Boolean,
    isActive: Boolean,
    distanceFromCurrent: Int,
    lyricsTextPosition: LyricsPosition,
    textColor: Color,
    textSize: Float,
    lineSpacing: Float,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean,
    isSelectionModeActive: Boolean,
    isAutoScrollActive: Boolean,
    expressiveAccent: Color,
    modifier: Modifier = Modifier
) {
    val (appleMusicLyricsBlur) = rememberPreference(AppleMusicLyricsBlurKey, true)

    val targetBlur = if (!appleMusicLyricsBlur || !isAutoScrollActive || isActive || !isSynced || isSelectionModeActive) {
        0f
    } else {
        when (distanceFromCurrent) {
            1 -> 0f
            2 -> 0f
            3 -> 2f
            4 -> 4f
            else -> 6f
        }
    }

    val animatedBlur by animateFloatAsState(
        targetValue = targetBlur,
        animationSpec = tween(durationMillis = 1000), label = "blur"
    )

    val duration = remember(entry.time, nextEntryTime) {
        if (nextEntryTime != null) nextEntryTime - entry.time else 4000L
    }

    val activeDuration = remember(duration) {
        (duration * 0.95).toLong().coerceAtLeast(300L)
    }

    val wordData = remember(entry.text, entry.words, activeDuration) {
        val isIndic = entry.text.any { it in '\u0900'..'\u0D7F' }
        if (!isIndic && entry.words != null && entry.words.isNotEmpty()) {
            entry.wordsWithSpacing().mapIndexed { index, word ->
                val wordStart = ((word.startTime * 1000).toLong() - entry.time).coerceAtLeast(0L)
                val wordEnd = ((word.endTime * 1000).toLong() - entry.time).coerceAtLeast(wordStart + 50L)
                Triple(word.text, wordStart, wordEnd)
            }
        } else {
            val words = entry.text.split(" ").filter { it.isNotEmpty() }
            if (words.isEmpty()) {
                listOf(Triple(entry.text, 0L, activeDuration))
            } else {
                val totalChars = entry.text.length
                var accumulatedTime = 0L
                words.mapIndexed { index, word ->
                    val wordLength = word.length
                    val includeSpace = index < words.size - 1
                    val charCount = if (includeSpace) wordLength + 1 else wordLength
                    val wordStart = accumulatedTime
                    val wordDur = if (totalChars > 0) (activeDuration * charCount.toFloat() / totalChars).toLong() else activeDuration
                    val wordEnd = wordStart + wordDur
                    accumulatedTime += wordDur
                    Triple(word + if (includeSpace) " " else "", wordStart, wordEnd)
                }
            }
        }
    }

    val targetAlpha = when {
        !isSynced || (isSelectionModeActive && isSelected) -> 1f
        isActive -> 1f
        distanceFromCurrent == 1 -> 0.65f
        distanceFromCurrent == 2 -> 0.45f
        else -> 0.35f
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 300),
        label = "lineAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 1f,
        animationSpec = tween(durationMillis = 400),
        label = "lineScale"
    )

    val itemModifier = modifier
        .fillMaxWidth()
        .graphicsLayer {
            this.alpha = animatedAlpha
            this.scaleX = scale
            this.scaleY = scale
        }
        .clip(RoundedCornerShape(16.dp))
        .combinedClickable(
            enabled = true,
            onClick = onClick,
            onLongClick = onLongClick
        )
        .background(
            if (isSelected && isSelectionModeActive)
                AppTheme.colorScheme.primary.copy(alpha = 0.2f)
            else Color.Transparent
        )
        .padding(horizontal = 24.dp, vertical = (8 * lineSpacing).dp)
        .blur(animatedBlur.dp)

    val agentAlignment = singerLineAlignment(entry.agent, entry.isBackground, lyricsTextPosition)
    val agentTextAlign = singerTextAlign(entry.agent, entry.isBackground, lyricsTextPosition)

    Column(
        modifier = itemModifier,
        horizontalAlignment = agentAlignment
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = when (agentTextAlign) {
                TextAlign.Center -> Arrangement.Center
                TextAlign.Right -> Arrangement.End
                else -> Arrangement.Start
            },
            verticalArrangement = Arrangement.spacedBy(
                with(LocalDensity.current) { (textSize * (lineSpacing.coerceAtMost(1.3f) - 1f)).sp.toDp() }
            )
        ) {
            wordData.forEachIndexed { index, (wordText, startRelative, endRelative) ->
                val lineRelTime = (effectivePlaybackPosition - entry.time).coerceAtLeast(0L)
                val wordDuration = (endRelative - startRelative).coerceAtLeast(1L)

                val progress by animateFloatAsState(
                    targetValue = when {
                        lineRelTime >= endRelative -> 1f
                        lineRelTime < startRelative -> 0f
                        else -> (lineRelTime - startRelative).toFloat() / wordDuration
                    },
                    animationSpec = tween(durationMillis = 150, easing = androidx.compose.animation.core.LinearEasing),
                    label = "wordProgress"
                )

                val finalFontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold

                Text(
                    fontFamily = fontFamily,
                    text = wordText.trimEnd(),
                    modifier = Modifier.padding(end = with(LocalDensity.current) {
                        (textSize * 0.25f * wordText.takeLastWhile { it.isWhitespace() }.length).sp.toDp()
                    }),
                    fontSize = textSize.sp,
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            0.0f to textColor,
                            (progress - 0.05f).coerceAtLeast(0f) to textColor,
                            (progress + 0.05f).coerceAtMost(1f) to textColor.copy(alpha = 0.45f),
                            1.0f to textColor.copy(alpha = 0.45f)
                        ),
                        fontWeight = finalFontWeight,
                        letterSpacing = (textSize * 0.012f).sp,
                        lineHeight = (textSize * lineSpacing.coerceAtMost(1.3f)).sp,
                        textAlign = agentTextAlign,
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = textColor.copy(alpha = 0.6f * progress),
                            offset = Offset.Zero,
                            blurRadius = (12f * progress).coerceAtLeast(0.1f)
                        )
                    )
                )
            }
        }
    }
}
