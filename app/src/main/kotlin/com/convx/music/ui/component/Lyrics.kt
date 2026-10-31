/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.component

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.text.Layout
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.convx.music.ui.ios.compat.BasicAlertDialog
import com.convx.music.ui.ios.compat.Button
import com.convx.music.ui.ios.compat.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import com.convx.music.ui.ios.compat.FilledTonalButton
import com.convx.music.ui.ios.compat.Icon
import com.convx.music.ui.ios.compat.AppTheme
import com.convx.music.ui.ios.compat.Text
import com.convx.music.ui.utils.VibrantChromeInk
import com.convx.music.ui.utils.vibrantChrome
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.convx.music.LocalDatabase
import com.convx.music.LocalPlayerConnection
import com.convx.music.R
import com.convx.music.constants.DarkModeKey
import com.convx.music.constants.LyricsClickKey
import com.convx.music.constants.LyricsGlowEffectKey
import com.convx.music.constants.LyricsLineSpacingKey
import com.convx.music.constants.LyricsStandardBlurKey
import com.convx.music.constants.LyricsScrollKey
import com.convx.music.constants.LyricsTextPositionKey
import com.convx.music.constants.LyricsTextSizeKey
import com.convx.music.constants.PlayerBackgroundStyle
import com.convx.music.constants.PlayerBackgroundStyleKey
import com.convx.music.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import com.convx.music.lyrics.LyricsEntry
import com.convx.music.lyrics.isSingerSectionStart
import com.convx.music.lyrics.primaryAgentId
import com.convx.music.lyrics.LyricsUtils.findCurrentLineIndex
import com.convx.music.lyrics.LyricsUtils.parseLyrics
import com.convx.music.ui.component.shimmer.ShimmerHost
import com.convx.music.ui.component.shimmer.TextPlaceholder
import com.convx.music.ui.screens.settings.DarkMode
import com.convx.music.ui.screens.settings.LyricsPosition
import com.convx.music.ui.utils.Motion
import com.convx.music.ui.utils.fadingEdge
import com.convx.music.utils.ComposeToImage
import com.convx.music.utils.rememberEnumPreference
import com.convx.music.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds
import androidx.lifecycle.compose.collectAsStateWithLifecycle


/** Centers the active line about a third down the pane and settles before the next line. */

@Composable
private fun ShareLyricsDialog(
    onDismiss: () -> Unit,
    onShareText: () -> Unit,
    onShareImage: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Card(
            shape = AppTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp).fillMaxWidth(0.85f),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.share_lyrics),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AppTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(16.dp))
                ShareLyricsOption(
                    text = stringResource(R.string.share_as_text),
                    onClick = onShareText,
                )
                ShareLyricsOption(
                    text = stringResource(R.string.share_as_image),
                    onClick = onShareImage,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontSize = 16.sp,
                        color = AppTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(onClick = onDismiss)
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareLyricsOption(
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SymbolIcon(symbol = sf("square.and.arrow.up"), contentDescription = null, tint = AppTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = AppTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun LyricsBottomControls(
    modifier: Modifier = Modifier,
    showAutoScroll: Boolean,
    isSelectionModeActive: Boolean,
    hasSelection: Boolean,
    onAutoScroll: () -> Unit,
    onCancelSelection: () -> Unit,
    onShareSelection: () -> Unit,
) {
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = showAutoScroll && !isSelectionModeActive,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            FilledTonalButton(onClick = onAutoScroll) {
                SymbolIcon(symbol = sf("arrow.triangle.2.circlepath"), contentDescription = stringResource(R.string.auto_scroll), size = 20.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.auto_scroll))
            }
        }

        AnimatedVisibility(
            visible = isSelectionModeActive,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalButton(onClick = onCancelSelection) {
                    SymbolIcon(symbol = sf("xmark"), contentDescription = stringResource(R.string.cancel), size = 20.dp)
                }
                FilledTonalButton(onClick = onShareSelection, enabled = hasSelection) {
                    SymbolIcon(symbol = sf("square.and.arrow.up"), contentDescription = stringResource(R.string.share_selected), size = 20.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.share))
                }
            }
        }
    }
}

@Composable
private fun LyricsColorSwatches(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp),
    ) {
        colors.distinct().take(8).forEach { color ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color, shape = RoundedCornerShape(8.dp))
                    .clickable { onColorSelected(color) }
                    .border(
                        2.dp,
                        if (selectedColor == color) AppTheme.colorScheme.primary else Color.Transparent,
                        RoundedCornerShape(8.dp),
                    ),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("UnusedBoxWithConstraintsScope", "StringFormatInvalid")
@Composable
fun Lyrics(
    sliderPositionProvider: () -> Long?,
    modifier: Modifier = Modifier,
    showLyrics: Boolean,
    // Changing this key resets the list and glides to the active line. Zero disables the intro.
    introKey: Int = 0,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val database = LocalDatabase.current.get()
    val menuState = LocalMenuState.current
    val density = LocalDensity.current
    val context = LocalContext.current
    val configuration = LocalWindowInfo.current
    val isGuest = false

    val lyricsTextPosition by rememberEnumPreference(LyricsTextPositionKey, LyricsPosition.LEFT)
    val changeLyrics by rememberPreference(LyricsClickKey, true)
    val scrollLyrics by rememberPreference(LyricsScrollKey, true)
    val lyricsGlowEffect by rememberPreference(LyricsGlowEffectKey, true)
    val lyricsFontSelection by rememberPreference(com.convx.music.constants.LyricsFontKey, "default")
    val lyricsFontFamily = com.convx.music.lyrics.rememberLyricsFont(lyricsFontSelection)
    val lyricsTextSize by rememberPreference(LyricsTextSizeKey, 30f)
    val lyricsLineSpacing by rememberPreference(LyricsLineSpacingKey, 1.3f)
    val lyricsStandardBlur by rememberPreference(LyricsStandardBlurKey, true)
    
    val scope = rememberCoroutineScope()

    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()
    val lyricsEntity by playerConnection.currentLyrics.collectAsState(initial = null)
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)
    val lyrics = remember(lyricsEntity) { lyricsEntity?.lyrics?.trim() }

    val playerBackground by rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = PlayerBackgroundStyle.GRADIENT
    )

    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    val lines = remember(lyrics) {
        if (lyrics == null || lyrics == LYRICS_NOT_FOUND) {
            emptyList()
        } else {
            val hasTimestamps = lyrics.startsWith("[")
            val entries = if (hasTimestamps) {
                parseLyrics(lyrics)
            } else {
                lyrics.lines().mapIndexed { index, line -> LyricsEntry(index * 100L, line) }
            }
            if (hasTimestamps) {
                listOf(LyricsEntry.HEAD_LYRICS_ENTRY) + entries
            } else {
                entries
            }
        }
    }
    val isSynced =
        remember(lyrics) {
            !lyrics.isNullOrEmpty() && lyrics.startsWith("[")
        }

    // Use one accent color for the lyric text and glow.
    val expressiveAccent = when (playerBackground) {
        PlayerBackgroundStyle.DEFAULT -> AppTheme.colorScheme.primary
        PlayerBackgroundStyle.BLUR, PlayerBackgroundStyle.GRADIENT, PlayerBackgroundStyle.GLOW_ANIMATED, PlayerBackgroundStyle.APPLE_MUSIC, PlayerBackgroundStyle.LIVE_MESH, PlayerBackgroundStyle.FLUID_MESH, PlayerBackgroundStyle.STATIC, PlayerBackgroundStyle.CUSTOM_GRADIENT, PlayerBackgroundStyle.GLASS -> {
            // For animated or processed backgrounds, always use light colors regardless of theme
            Color.White
        }
    }
    val textColor = expressiveAccent

    var currentLineIndex by remember {
        mutableIntStateOf(-1)
    }
    var currentPlaybackPosition by remember {
        mutableLongStateOf(0L)
    }
    // Avoid stale highlighting while LaunchedEffect is waiting during a scroll.
    var deferredCurrentLineIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    var previousLineIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    var lastPreviewTime by rememberSaveable {
        mutableLongStateOf(0L)
    }
    var isSeeking by remember {
        mutableStateOf(false)
    }

    var initialScrollDone by rememberSaveable {
        mutableStateOf(false)
    }

    var shouldScrollToFirstLine by rememberSaveable {
        mutableStateOf(true)
    }

    var isAppMinimized by rememberSaveable {
        mutableStateOf(false)
    }

    var showProgressDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var shareDialogData by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    var showColorPickerDialog by remember { mutableStateOf(false) }
    var previewBackgroundColor by remember { mutableStateOf(Color(0xFF242424)) }
    var previewTextColor by remember { mutableStateOf(Color.White) }
    var previewSecondaryTextColor by remember { mutableStateOf(Color.White.copy(alpha = 0.7f)) }

    var isSelectionModeActive by rememberSaveable { mutableStateOf(false) }
    val selectedIndices = remember { mutableStateListOf<Int>() }
    var showMaxSelectionToast by remember { mutableStateOf(false) }

    val isLyricsProviderShown = lyricsEntity?.provider != null && lyricsEntity?.provider != "Unknown" && !isSelectionModeActive
    val hasProviderRow = isLyricsProviderShown && true

    val lazyListState = rememberLazyListState()
    
    var isAutoScrollEnabled by rememberSaveable { mutableStateOf(true) }

    BackHandler(enabled = isSelectionModeActive) {
        isSelectionModeActive = false
        selectedIndices.clear()
    }

    val maxSelectionLimit = 5

    LaunchedEffect(showMaxSelectionToast) {
        if (showMaxSelectionToast) {
            Toast.makeText(
                context,
                context.getString(R.string.max_selection_limit, maxSelectionLimit),
                Toast.LENGTH_SHORT
            ).show()
            showMaxSelectionToast = false
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    // Keep screen on while lyrics are visible
    DisposableEffect(showLyrics) {
        val activity = context as? Activity
        if (showLyrics) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                val visibleItemsInfo = lazyListState.layoutInfo.visibleItemsInfo
                val visibleLineIndex = currentLineIndex + if (hasProviderRow) 1 else 0
                val isCurrentLineVisible = currentLineIndex >= 0 && visibleItemsInfo.any { it.index == visibleLineIndex }
                if (isCurrentLineVisible) {
                    initialScrollDone = false
                }
                isAppMinimized = true
            } else if(event == Lifecycle.Event.ON_START) {
                isAppMinimized = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Reset selection mode if lyrics change
    LaunchedEffect(lines) {
        isSelectionModeActive = false
        selectedIndices.clear()
    }

    LaunchedEffect(lyrics) {
        if (lyrics.isNullOrEmpty() || !lyrics.startsWith("[")) {
            currentLineIndex = -1
            return@LaunchedEffect
        }
        while (isActive) {
            delay(100) // Line positions only need ~10Hz; word animation reads this position
            val sliderPosition = sliderPositionProvider()
            isSeeking = sliderPosition != null
            val position = sliderPosition ?: playerConnection.player.currentPosition
            currentPlaybackPosition = position
            val lyricsOffset = currentSong?.song?.lyricsOffset ?: 0
            val newLineIndex = com.convx.music.lyrics.primaryLyricLineIndex(lines, position + lyricsOffset)
            if (newLineIndex != currentLineIndex) {
                currentLineIndex = newLineIndex
            }
        }
    }

    val isUserDragging by lazyListState.interactionSource.collectIsDraggedAsState()
    LaunchedEffect(lastPreviewTime, isUserDragging, isSelectionModeActive, scrollLyrics) {
        if (lastPreviewTime != 0L && !isUserDragging && !isSelectionModeActive) {
            delay(LyricsPreviewTime)
            lastPreviewTime = 0L
            isAutoScrollEnabled = true
        }
    }

    suspend fun performSmoothPageScroll(targetIndex: Int, animate: Boolean = true) {
        if (targetIndex < 0) return
        lazyListState.followLyricLine(targetIndex + if (hasProviderRow) 1 else 0, animate)
    }
    LaunchedEffect(introKey) {
        if (introKey == 0) return@LaunchedEffect
        // Keep the current playback position. Opening lyrics should reveal the
        // active line directly instead of first showing the top and gliding down.
        initialScrollDone = false
    }
    LaunchedEffect(currentLineIndex, lastPreviewTime, initialScrollDone, isAutoScrollEnabled, scrollLyrics, isSeeking, hasProviderRow, lines, lazyListState.layoutInfo.totalItemsCount) {
        if (!isSynced || !scrollLyrics) return@LaunchedEffect
        // The playhead-to-line lookup starts asynchronously. Don't interpret its
        // initial -1 as line zero and briefly jump to the start of the lyrics.
        if (currentLineIndex < 0) return@LaunchedEffect

        if (isAutoScrollEnabled) {
            val shouldCenterFirstLine =
                (currentLineIndex == 0 && shouldScrollToFirstLine) || !initialScrollDone
            if (shouldCenterFirstLine) {
                shouldScrollToFirstLine = false
                performSmoothPageScroll(currentLineIndex, animate = false)
                if (!isAppMinimized) {
                    initialScrollDone = true
                }
            } else {
                deferredCurrentLineIndex = currentLineIndex
                if (isSeeking) {
                    performSmoothPageScroll(currentLineIndex.coerceAtLeast(0))
                } else if (scrollLyrics && currentLineIndex != previousLineIndex) {
                    performSmoothPageScroll(currentLineIndex)
                }
            }
        }

        if (currentLineIndex > 0) {
            shouldScrollToFirstLine = true
        }
        previousLineIndex = currentLineIndex
    }

    BoxWithConstraints(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 12.dp)
    ) {


        if (lyrics == LYRICS_NOT_FOUND) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    fontFamily = lyricsFontFamily,
                    text = stringResource(R.string.lyrics_not_found),
                    fontSize = 20.sp,
                    color = AppTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.alpha(0.5f)
                )
            }
        } else {
            val lyricsOffset = currentSong?.song?.lyricsOffset?.toLong() ?: 0L
            val effectivePlaybackPosition = currentPlaybackPosition + lyricsOffset

            val singerNames = remember(lines, mediaMetadata?.artists) {
                resolveSingerNames(lines, mediaMetadata?.artists?.map { it.name } ?: emptyList())
            }

            val isCompactView = maxHeight < 500.dp
            val topInset = if (isCompactView) (maxHeight * 0.25f).coerceAtLeast(24.dp) else (maxHeight * 0.30f).coerceAtLeast(64.dp)
            val bottomInset = if (isCompactView) (maxHeight * 0.35f).coerceAtLeast(48.dp) else (maxHeight * 0.40f).coerceAtLeast(96.dp)

            LazyColumn(
                state = lazyListState,
                contentPadding = WindowInsets.systemBars
                    .only(WindowInsetsSides.Top)
                    .add(WindowInsets(top = topInset, bottom = bottomInset))
                    .asPaddingValues(),
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .fadingEdge(vertical = 48.dp)
                    .nestedScroll(remember {
                        object : NestedScrollConnection {
                            override fun onPostScroll(
                                consumed: Offset,
                                available: Offset,
                                source: NestedScrollSource
                            ): Offset {
                                if (source == NestedScrollSource.UserInput) {
                                    isAutoScrollEnabled = false
                                    if (!isSelectionModeActive) lastPreviewTime = System.currentTimeMillis()
                                }
                                return super.onPostScroll(consumed, available, source)
                            }

                            override suspend fun onPostFling(
                                consumed: Velocity,
                                available: Velocity
                            ): Velocity {
                                isAutoScrollEnabled = false
                                if (!isSelectionModeActive) { // Only update preview time if not selecting
                                    lastPreviewTime = System.currentTimeMillis()
                                }
                                return super.onPostFling(consumed, available)
                            }
                        }
                    })
            ) {
                val displayedCurrentLineIndex = if (!isAutoScrollEnabled) {
                    currentLineIndex
                } else {
                    if (isSeeking || isSelectionModeActive) deferredCurrentLineIndex else currentLineIndex
                }

                if (hasProviderRow) {
                    item {
                        Text(
                    fontFamily = lyricsFontFamily,
                            text = "Lyrics from ${lyricsEntity?.provider}",
                            fontSize = 12.sp,
                            color = AppTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                if (lyrics == null) {
                    item {
                        ShimmerHost {
                            repeat(10) {
                                Box(
                                    contentAlignment = when (lyricsTextPosition) {
                                        LyricsPosition.LEFT -> Alignment.CenterStart
                                        LyricsPosition.CENTER -> Alignment.Center
                                        LyricsPosition.RIGHT -> Alignment.CenterEnd
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 4.dp)
                                ) {
                                    TextPlaceholder()
                                }
                            }
                        }
                    }
                } else {
                    itemsIndexed(
                        items = lines,
                        key = { index, item -> "$index-${item.time}" } // Add stable key
                    ) { index, item ->
                    val isSelected = selectedIndices.contains(index)
                    val currentLineTime = if (displayedCurrentLineIndex >= 0 && displayedCurrentLineIndex < lines.size) {
                        lines[displayedCurrentLineIndex].time
                    } else -1L
                    val isLineAtSameTime = item.time == currentLineTime
                    val isActiveByIndex = index == displayedCurrentLineIndex
                    val isActiveByTime = isLineAtSameTime && displayedCurrentLineIndex >= 0

                    ConvxLyricsLine(
                        fontFamily = lyricsFontFamily,
                        entry = item,
                        nextEntryTime = lines.getOrNull(index + 1)?.time,
                        effectivePlaybackPosition = effectivePlaybackPosition,
                        isSynced = isSynced,
                        isActive = isActiveByIndex || isActiveByTime,
                        distanceFromCurrent = kotlin.math.abs(index - displayedCurrentLineIndex),
                        lyricsTextPosition = lyricsTextPosition,
                        textColor = textColor,
                        textSize = lyricsTextSize,
                        lineSpacing = lyricsLineSpacing,
                        isAutoScrollActive = isAutoScrollEnabled,
                        isSelectionModeActive = isSelectionModeActive,
                        isSelected = isSelected,
                        expressiveAccent = expressiveAccent,
                        onClick = {
                            if (isSelectionModeActive) {
                                if (isSelected) {
                                    selectedIndices.remove(index)
                                    if (selectedIndices.isEmpty()) isSelectionModeActive = false
                                } else {
                                    if (selectedIndices.size < maxSelectionLimit) selectedIndices.add(index)
                                    else showMaxSelectionToast = true
                                }
                            } else if (isSynced && changeLyrics && !isGuest) {
                                val lyricsOffset = currentSong?.song?.lyricsOffset ?: 0
                                playerConnection.seekTo((item.time - lyricsOffset).coerceAtLeast(0))
                                currentLineIndex = index
                                isAutoScrollEnabled = true
                                lastPreviewTime = 0L
                            }
                        },
                        onLongClick = {
                            if (!isSelectionModeActive) {
                                isSelectionModeActive = true
                                selectedIndices.add(index)
                            } else if (!isSelected && selectedIndices.size < maxSelectionLimit) {
                                selectedIndices.add(index)
                            } else if (!isSelected) {
                                showMaxSelectionToast = true
                            }
                        }
                    )
                }
            }
        }
    }

    LyricsBottomControls(
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
        showAutoScroll = !isAutoScrollEnabled && isSynced,
        isSelectionModeActive = isSelectionModeActive,
        hasSelection = selectedIndices.isNotEmpty(),
        onAutoScroll = {
            isAutoScrollEnabled = true
        },
        onCancelSelection = {
            isSelectionModeActive = false
            selectedIndices.clear()
        },
        onShareSelection = {
            if (selectedIndices.isNotEmpty()) {
                val selectedLyricsText = selectedIndices.sorted()
                    .mapNotNull { lines.getOrNull(it)?.text }
                    .joinToString("\n")

                if (selectedLyricsText.isNotBlank()) {
                    shareDialogData = Triple(
                        selectedLyricsText,
                        mediaMetadata?.title ?: "",
                        mediaMetadata?.artists?.joinToString { it.name } ?: "",
                    )
                    showShareDialog = true
                }
                isSelectionModeActive = false
                selectedIndices.clear()
            }
        },
    )

    if (showProgressDialog) {
        BasicAlertDialog(onDismissRequest = { }) {
            Card(
                shape = AppTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = AppTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.padding(32.dp)) {
                    Text(
                    fontFamily = lyricsFontFamily,
                        text = stringResource(R.string.generating_image) + "\n" + stringResource(R.string.please_wait),
                        color = AppTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    shareDialogData?.takeIf { showShareDialog }?.let { (lyricsText, songTitle, artists) ->
        ShareLyricsDialog(
            onDismiss = { showShareDialog = false },
            onShareText = {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    val songLink = com.convx.music.utils.ShareUtils.songUrl(mediaMetadata?.id ?: "")
                    putExtra(Intent.EXTRA_TEXT, "\"$lyricsText\"\n\n$songTitle - $artists\n$songLink")
                }
                context.startActivity(
                    Intent.createChooser(shareIntent, context.getString(R.string.share_lyrics))
                )
                showShareDialog = false
            },
            onShareImage = {
                shareDialogData = Triple(lyricsText, songTitle, artists)
                showColorPickerDialog = true
                showShareDialog = false
            },
        )
    }

    val colorPickerData = shareDialogData
    if (showColorPickerDialog && colorPickerData != null) {
        val (lyricsText, songTitle, artists) = colorPickerData
        val coverUrl = mediaMetadata?.thumbnailUrl
        val paletteColors = remember { mutableStateListOf<Color>() }
        
        var previewBackgroundStyle by remember { mutableStateOf(LyricsBackgroundStyle.SOLID) }

        val previewCardWidth = configuration.containerDpSize.width * 0.90f
        val previewPadding = 20.dp * 2
        val previewBoxPadding = 28.dp * 2
        val previewAvailableWidth = previewCardWidth - previewPadding - previewBoxPadding
        val previewBoxHeight = 340.dp
        val headerFooterEstimate = (48.dp + 14.dp + 16.dp + 20.dp + 8.dp + 28.dp * 2)
        val previewAvailableHeight = previewBoxHeight - headerFooterEstimate

        val lyricsTextAlign = when (lyricsTextPosition) {
            LyricsPosition.LEFT -> TextAlign.Left
            LyricsPosition.CENTER -> TextAlign.Center
            LyricsPosition.RIGHT -> TextAlign.Right
        }

        val textStyleForMeasurement = TextStyle(
            color = previewTextColor,
            fontWeight = FontWeight.Bold,
            textAlign = lyricsTextAlign
        )
        val textMeasurer = rememberTextMeasurer()

        rememberAdjustedFontSize(
            text = lyricsText,
            maxWidth = previewAvailableWidth,
            maxHeight = previewAvailableHeight,
            density = density,
            initialFontSize = 50.sp,
            minFontSize = 22.sp,
            style = textStyleForMeasurement,
            textMeasurer = textMeasurer
        )

        LaunchedEffect(coverUrl) {
            if (coverUrl != null) {
                withContext(Dispatchers.IO) {
                    try {
                        val loader = ImageLoader(context)
                        val req = ImageRequest.Builder(context).data(coverUrl).allowHardware(false).build()
                        val result = loader.execute(req)
                        val bmp = result.image?.toBitmap()
                        if (bmp != null) {
                            val palette = Palette.from(bmp).generate()
                            val swatches = palette.swatches.sortedByDescending { it.population }
                            val colors = swatches.map { Color(it.rgb) }
                                .filter { color ->
                                    val hsv = FloatArray(3)
                                    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
                                    hsv[1] > 0.2f
                                }
                            paletteColors.clear()
                            paletteColors.addAll(colors.take(5))
                        }
                    } catch (_: Exception) {
                        // Lyrics remain readable when artwork palette extraction fails.
                    }
                }
            }
        }

        BasicAlertDialog(onDismissRequest = { showColorPickerDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Text(
                    fontFamily = lyricsFontFamily,
                        text = stringResource(id = R.string.customize_colors),
                        style = AppTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = stringResource(id = R.string.player_background_style), style = AppTheme.typography.titleMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        LyricsBackgroundStyle.entries.forEach { style ->
                            val label = when(style) {
                                LyricsBackgroundStyle.SOLID -> stringResource(R.string.player_background_solid)
                                LyricsBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                                LyricsBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                            }
                            val selected = previewBackgroundStyle == style
                            
                            androidx.compose.material3.FilterChip(
                                selected = selected,
                                onClick = { previewBackgroundStyle = style },
                                label = { Text(label) }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        LyricsImageCard(
                            lyricText = lyricsText,
                            mediaMetadata = mediaMetadata ?: return@Box,
                            backgroundColor = previewBackgroundColor,
                            backgroundStyle = previewBackgroundStyle,
                            textColor = previewTextColor,
                                secondaryTextColor = previewSecondaryTextColor,
                                textAlign = lyricsTextAlign
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(text = stringResource(id = R.string.background_color), style = AppTheme.typography.titleMedium)
                    LyricsColorSwatches(
                        colors = paletteColors + listOf(Color(0xFF242424), Color(0xFF121212), Color.White, Color.Black, Color(0xFFF5F5F5)),
                        selectedColor = previewBackgroundColor,
                        onColorSelected = { previewBackgroundColor = it },
                    )

                    Text(text = stringResource(id = R.string.text_color), style = AppTheme.typography.titleMedium)
                    LyricsColorSwatches(
                        colors = paletteColors + listOf(Color.White, Color.Black, Color(0xFF1DB954)),
                        selectedColor = previewTextColor,
                        onColorSelected = { previewTextColor = it },
                    )

                    Text(text = stringResource(id = R.string.secondary_text_color), style = AppTheme.typography.titleMedium)
                    LyricsColorSwatches(
                        colors = paletteColors.map { it.copy(alpha = 0.7f) } +
                            listOf(Color.White.copy(alpha = 0.7f), Color.Black.copy(alpha = 0.7f), Color(0xFF1DB954)),
                        selectedColor = previewSecondaryTextColor,
                        onColorSelected = { previewSecondaryTextColor = it },
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            showColorPickerDialog = false
                            showProgressDialog = true
                            scope.launch {
                                try {
                                    val screenWidth = configuration.containerSize.width
                                    val screenHeight = configuration.containerSize.height

                                    val image = ComposeToImage.createLyricsImage(
                                        context = context,
                                        coverArtUrl = coverUrl,
                                        songTitle = songTitle,
                                        artistName = artists,
                                        lyrics = lyricsText,
                                        width = (screenWidth * density.density).toInt(),
                                        height = (screenHeight * density.density).toInt(),
                                        backgroundColor = previewBackgroundColor.toArgb(),
                                        backgroundStyle = previewBackgroundStyle,
                                        textColor = previewTextColor.toArgb(),
                                        secondaryTextColor = previewSecondaryTextColor.toArgb(),
                                        lyricsAlignment = when (lyricsTextPosition) {
                                            LyricsPosition.LEFT -> Layout.Alignment.ALIGN_NORMAL
                                            LyricsPosition.CENTER -> Layout.Alignment.ALIGN_CENTER
                                            LyricsPosition.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
                                        }
                                    )
                                    val timestamp = System.currentTimeMillis()
                                    val filename = "lyrics_$timestamp"
                                    val uri = ComposeToImage.saveBitmapAsFile(context, image, filename)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_lyrics)))
                                } catch (e: Exception) {
                                    Toast.makeText(context, context.getString(R.string.failed_to_create_image, e.message), Toast.LENGTH_SHORT).show()
                                } finally {
                                    showProgressDialog = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(id = R.string.share))
                    }
                }
            }
        }
        } //else block
    }
}

val LyricsPreviewTime = 2.seconds
