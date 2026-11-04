/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.ExperimentalMaterial3Api
import com.convx.music.ui.ios.compat.ModalBottomSheet
import com.convx.music.ui.ios.compat.rememberIosModalSheetState
import com.convx.music.ui.ios.foundation.IosTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.convx.music.ui.utils.appTopBarWindowInsets
import android.app.Activity
import android.os.Build
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import android.content.Context
import androidx.core.content.edit
import com.convx.music.ui.utils.bounceClick
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.convx.music.ui.ios.compat.Icon
import com.convx.music.ui.ios.compat.AppTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.layout.layout
import com.convx.music.ui.component.shapes.ContinuousCapsule
import com.convx.music.ui.component.shapes.ContinuousRoundedRectangle
import com.convx.music.ui.ios.foundation.IosMetrics
import com.convx.music.ui.ios.compat.HorizontalDivider
import com.convx.music.ui.ios.compat.Switch
import androidx.compose.material3.SwitchDefaults
import com.convx.music.ui.ios.compat.Text
import com.convx.music.ui.ios.compat.TextButton
import com.convx.music.ui.ios.compat.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.composables.sfsymbols.SfSymbols
import com.composables.sfsymbols.monochrome.SFGaugeWithNeedle
import com.composables.sfsymbols.monochrome.SFIcloudAndArrowDownFill
import com.composables.sfsymbols.monochrome.SFSliderHorizontal3
import com.composables.sfsymbols.monochrome.SFStar
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.convx.music.LocalPlayerAwareWindowInsets
import com.convx.music.R
import com.convx.music.constants.CropAlbumArtKey
import com.convx.music.constants.DefaultOpenTabKey
import com.convx.music.constants.DynamicThemeKey
import com.convx.music.constants.EnableHighRefreshRateKey
import com.convx.music.constants.HideSystemBarsKey
import com.convx.music.constants.OneTapFullscreenLyricsKey
import com.convx.music.constants.HomeHeroCardEnabledKey
import com.convx.music.constants.PureBlackHeroBackgroundKey
import com.convx.music.constants.LiquidGlassGlobalEnabledKey
import com.convx.music.constants.AppLanguageKey
import com.convx.music.constants.LanguageCodeToName
import com.convx.music.constants.SYSTEM_DEFAULT
import com.convx.music.ui.ios.compat.Slider
import com.convx.music.constants.LyricsTextPositionKey
import com.convx.music.constants.LyricsTextSizeKey
import com.convx.music.constants.ShowCachedPlaylistKey
import com.convx.music.constants.ShowDownloadedPlaylistKey
import com.convx.music.constants.ShowLikedPlaylistKey
import com.convx.music.constants.ShowTopPlaylistKey
import com.convx.music.constants.ShowUploadedPlaylistKey
import com.convx.music.constants.SlimNavBarKey
import com.convx.music.constants.SwipeSensitivityKey
import com.convx.music.constants.SwipeThumbnailKey
import com.convx.music.constants.SwipeLyricsKey
import com.convx.music.constants.SwipeToRemoveSongKey
import com.convx.music.constants.SwipeToSongKey
import com.convx.music.constants.UseNewMiniPlayerDesignKey
import com.convx.music.constants.UseNewPlayerDesignKey
import com.convx.music.ui.component.EnumDialog
import com.convx.music.ui.component.IconButton
import com.convx.music.ui.component.IosSettingsGroup
import com.convx.music.ui.component.IosSettingsItem
import com.convx.music.ui.utils.backToMain
import com.convx.music.utils.rememberEnumPreference
import com.convx.music.utils.rememberPreference
import kotlin.math.roundToInt
import com.convx.music.constants.LyricsClickKey
import com.convx.music.constants.LyricsLineSpacingKey
import com.convx.music.constants.LyricsScrollKey
import com.convx.music.constants.MiniPlayerWaveformKey
import com.convx.music.constants.AppFont
import com.convx.music.constants.SelectedFontKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    activity: Activity,
    snackbarHostState: SnackbarHostState,
) {
    val (_, _) = rememberPreference(UseNewMiniPlayerDesignKey, defaultValue = true)
    val (_, _) = rememberPreference(DynamicThemeKey, defaultValue = true)
    val (useNewPlayerDesign, _) = rememberPreference(UseNewPlayerDesignKey, defaultValue = false)
    val (miniPlayerWaveform, onMiniPlayerWaveformChange) = rememberPreference(
        MiniPlayerWaveformKey,
        defaultValue = false
    )
    val (selectedFontValue) = rememberPreference(
        SelectedFontKey,
        defaultValue = AppFont.PLUS_JAKARTA_SANS.value
    )

    val (enableHighRefreshRate, onEnableHighRefreshRateChange) = rememberPreference(
        EnableHighRefreshRateKey,
        defaultValue = true
    )
    val (hideSystemBars, onHideSystemBarsChange) = rememberPreference(
        HideSystemBarsKey,
        defaultValue = false
    )
    val (cropAlbumArt, onCropAlbumArtChange) = rememberPreference(
        CropAlbumArtKey,
        defaultValue = false
    )
    val (defaultOpenTab, onDefaultOpenTabChange) = rememberEnumPreference(
        DefaultOpenTabKey,
        defaultValue = NavigationTab.HOME
    )
    val (lyricsPosition, onLyricsPositionChange) = rememberEnumPreference(
        LyricsTextPositionKey,
        defaultValue = LyricsPosition.LEFT
    )
    val (lyricsClick, onLyricsClickChange) = rememberPreference(LyricsClickKey, defaultValue = true)
    val (lyricsScroll, onLyricsScrollChange) = rememberPreference(
        LyricsScrollKey,
        defaultValue = true
    )

    val (lyricsTextSize, onLyricsTextSizeChange) = rememberPreference(LyricsTextSizeKey, defaultValue = 30f)
    val (lyricsLineSpacing, onLyricsLineSpacingChange) = rememberPreference(LyricsLineSpacingKey, defaultValue = 1.3f)
    val (swipeLyrics, onSwipeLyricsChange) = rememberPreference(SwipeLyricsKey, defaultValue = false)
    val (oneTapFullscreenLyrics, onOneTapFullscreenLyricsChange) = rememberPreference(OneTapFullscreenLyricsKey, defaultValue = false)

    val (swipeThumbnail, onSwipeThumbnailChange) = rememberPreference(
        SwipeThumbnailKey,
        defaultValue = true
    )
    val (swipeSensitivity, onSwipeSensitivityChange) = rememberPreference(
        SwipeSensitivityKey,
        defaultValue = 0.73f
    )

    val (pureBlackHeroBackground, onPureBlackHeroBackgroundChange) = rememberPreference(PureBlackHeroBackgroundKey, false)
    val (liquidGlassEnabled, onLiquidGlassEnabledChange) = rememberPreference(
        LiquidGlassGlobalEnabledKey,
        defaultValue = true
    )

    // App language is a general setting, so keep it in the miscellaneous group.
    val (appLanguage, onAppLanguageChange) = rememberPreference(key = AppLanguageKey, defaultValue = SYSTEM_DEFAULT)
    var showAppLanguageDialog by rememberSaveable { mutableStateOf(false) }
    val (homeHeroCardEnabled, onHomeHeroCardEnabledChange) = rememberPreference(HomeHeroCardEnabledKey, true)



    val (swipeToSong, onSwipeToSongChange) = rememberPreference(
        SwipeToSongKey,
        defaultValue = false
    )

    val (swipeToRemoveSong, onSwipeToRemoveSongChange) = rememberPreference(
        SwipeToRemoveSongKey,
        defaultValue = false
    )

    val (showLikedPlaylist, onShowLikedPlaylistChange) = rememberPreference(
        ShowLikedPlaylistKey,
        defaultValue = true
    )
    val (showDownloadedPlaylist, onShowDownloadedPlaylistChange) = rememberPreference(
        ShowDownloadedPlaylistKey,
        defaultValue = true
    )
    val (showTopPlaylist, onShowTopPlaylistChange) = rememberPreference(
        ShowTopPlaylistKey,
        defaultValue = true
    )
    val (showCachedPlaylist, onShowCachedPlaylistChange) = rememberPreference(
        ShowCachedPlaylistKey,
        defaultValue = true
    )
    val (showUploadedPlaylist, onShowUploadedPlaylistChange) = rememberPreference(
        ShowUploadedPlaylistKey,
        defaultValue = true
    )


    var showDefaultOpenTabDialog by rememberSaveable { mutableStateOf(false) }
    if (showDefaultOpenTabDialog) {
        EnumDialog(
            onDismiss = { showDefaultOpenTabDialog = false },
            onSelect = {
                onDefaultOpenTabChange(it)
                showDefaultOpenTabDialog = false
            },
            title = stringResource(R.string.default_open_tab),
            current = defaultOpenTab,
            values = NavigationTab.entries,
            valueText = {
                when (it) {
                    NavigationTab.HOME -> stringResource(R.string.home)
                    NavigationTab.SEARCH -> stringResource(R.string.search)
                    NavigationTab.LIBRARY -> stringResource(R.string.filter_library)
                }
            }
        )
    }

    if (showAppLanguageDialog) {
        EnumDialog(
            onDismiss = { showAppLanguageDialog = false },
            onSelect = {
                onAppLanguageChange(it)
                showAppLanguageDialog = false
            },
            title = stringResource(R.string.app_language),
            current = appLanguage,
            values = (listOf(SYSTEM_DEFAULT) + LanguageCodeToName.keys.toList()),
            valueText = {
                LanguageCodeToName.getOrElse(it) { stringResource(R.string.system_default) }
            }
        )
    }

    SettingsLazyPage(
        navController = navController,
        title = stringResource(R.string.appearance),
        pageColor = IosTheme.colors.systemGroupedBackground,
        horizontalPadding = 16.dp,
    ) {
        item(key = "all_appearance_settings") {
            Column {
        IosSettingsGroup(
            title = "Theme & colors",
            items = listOf(
                IosSettingsItem(
                    icon = painterResource(R.drawable.palette),
                    title = { Text(stringResource(R.string.theme_colors)) },
                    onClick = { navController.navigate("settings/appearance/theme") }
                )
            )
        )

        Spacer(modifier = Modifier.height(27.dp))

        IosSettingsGroup(
            title = "App identity",
            items = listOf(
                IosSettingsItem(
                    icon = painterResource(R.drawable.alphabet_cyrillic),
                    title = { Text(stringResource(R.string.app_font)) },
                    description = {
                        val fontLabel = when (AppFont.fromValue(selectedFontValue)) {
                            AppFont.SYSTEM -> stringResource(R.string.font_system)
                            AppFont.GOOGLE_SANS -> stringResource(R.string.font_google_sans)
                            AppFont.SANS_FLEX -> stringResource(R.string.font_sans_flex)
                            AppFont.OUTFIT -> stringResource(R.string.font_outfit)
                            AppFont.PLUS_JAKARTA_SANS -> stringResource(R.string.font_plus_jakarta_sans)
                        }
                        Text(fontLabel)
                    },
                    onClick = { navController.navigate("settings/appearance/font") }
                )
            )
        )

        Spacer(modifier = Modifier.height(27.dp))

        IosSettingsGroup(
            title = stringResource(R.string.liquid_glass),
            items = listOf(
                IosSettingsItem(
                    icon = painterResource(R.drawable.tune),
                    title = { Text(stringResource(R.string.liquid_glass)) },
                    description = { Text(stringResource(R.string.liquid_glass_simple_desc)) },
                    trailingContent = {
                        Switch(
                            checked = liquidGlassEnabled,
                            onCheckedChange = onLiquidGlassEnabledChange
                        )
                    },
                    onClick = { onLiquidGlassEnabledChange(!liquidGlassEnabled) }
                ),
                IosSettingsItem(
                    icon = rememberVectorPainter(SfSymbols.Monochrome.SFSliderHorizontal3),
                    title = { Text(stringResource(R.string.advanced)) },
                    description = { Text(stringResource(R.string.advanced_settings_desc)) },
                    onClick = { navController.navigate("settings/appearance/liquidglass") }
                )
            )
        )

        Spacer(modifier = Modifier.height(27.dp))

        IosSettingsGroup(
            title = "Display",
            items = buildList {
                add(
                    IosSettingsItem(
                        icon = rememberVectorPainter(SfSymbols.Monochrome.SFGaugeWithNeedle),
                        title = { Text(stringResource(R.string.enable_high_refresh_rate)) },
                        description = { Text(stringResource(R.string.enable_high_refresh_rate_desc)) },
                        trailingContent = {
                            Switch(
                                checked = enableHighRefreshRate,
                                onCheckedChange = onEnableHighRefreshRateChange
                            )
                        },
                        onClick = { onEnableHighRefreshRateChange(!enableHighRefreshRate) }
                    )
                )
                add(
                    IosSettingsItem(
                        icon = painterResource(R.drawable.crop),
                        title = { Text("Hide status bar and navigation buttons") },
                        description = { Text("Hide status and navigation bars when the player is expanded; swipe from edge to show briefly") },
                        trailingContent = {
                            Switch(
                                checked = hideSystemBars,
                                onCheckedChange = onHideSystemBarsChange
                            )
                        },
                        onClick = { onHideSystemBarsChange(!hideSystemBars) }
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(27.dp))

        IosSettingsGroup(
            title = "Player display",
            items = listOfNotNull(
                IosSettingsItem(
                    icon = painterResource(R.drawable.gradient),
                    title = { Text(stringResource(R.string.player_theme)) },
                    description = { Text(stringResource(R.string.player_theme_desc)) },
                    onClick = { navController.navigate("settings/appearance/playertheme") }
                ),
                IosSettingsItem(
                    icon = painterResource(R.drawable.crop),
                    title = { Text(stringResource(R.string.crop_album_art)) },
                    description = { Text(stringResource(R.string.crop_album_art_desc)) },
                    trailingContent = {
                        Switch(
                            checked = cropAlbumArt,
                            onCheckedChange = onCropAlbumArtChange
                        )
                    },
                    onClick = { onCropAlbumArtChange(!cropAlbumArt) }
                ),
                IosSettingsItem(
                    icon = painterResource(R.drawable.tune),
                    title = { Text(stringResource(R.string.mini_player_waveform)) },
                    trailingContent = {
                        Switch(
                            checked = miniPlayerWaveform,
                            onCheckedChange = onMiniPlayerWaveformChange
                        )
                    },
                    onClick = { onMiniPlayerWaveformChange(!miniPlayerWaveform) }
                )
            )
        )

        Spacer(modifier = Modifier.height(27.dp))



        IosSettingsGroup(
            title = "More",
            items = listOf(
                IosSettingsItem(
                    icon = painterResource(R.drawable.nav_bar),
                    title = { Text(stringResource(R.string.default_open_tab)) },
                    description = {
                        Text(
                            when (defaultOpenTab) {
                                NavigationTab.HOME -> stringResource(R.string.home)
                                NavigationTab.SEARCH -> stringResource(R.string.search)
                                NavigationTab.LIBRARY -> stringResource(R.string.filter_library)
                            }
                        )
                    },
                    onClick = { showDefaultOpenTabDialog = true }
                ),
                IosSettingsItem(
                    icon = painterResource(R.drawable.swipe),
                    title = { Text(stringResource(R.string.swipe_song_to_add)) },
                    trailingContent = {
                        Switch(
                            checked = swipeToSong,
                            onCheckedChange = onSwipeToSongChange
                        )
                    },
                    onClick = { onSwipeToSongChange(!swipeToSong) }
                ),
                IosSettingsItem(
                    icon = painterResource(R.drawable.swipe),
                    title = { Text(stringResource(R.string.swipe_song_to_remove)) },
                    trailingContent = {
                        Switch(
                            checked = swipeToRemoveSong,
                            onCheckedChange = onSwipeToRemoveSongChange
                        )
                    },
                    onClick = { onSwipeToRemoveSongChange(!swipeToRemoveSong) }
                ),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    IosSettingsItem(
                        icon = painterResource(R.drawable.language),
                        title = { Text(stringResource(R.string.app_language)) },
                        onClick = {
                            activity.startActivity(
                                Intent(
                                    Settings.ACTION_APP_LOCALE_SETTINGS,
                                    "package:${activity.packageName}".toUri()
                                )
                            )
                        }
                    )
                } else {
                    IosSettingsItem(
                        icon = painterResource(R.drawable.language),
                        title = { Text(stringResource(R.string.app_language)) },
                        description = {
                            Text(
                                LanguageCodeToName.getOrElse(appLanguage) { stringResource(R.string.system_default) }
                            )
                        },
                        onClick = { showAppLanguageDialog = true }
                    )
                }
            )
        )

        Spacer(modifier = Modifier.height(27.dp))

        IosSettingsGroup(
            title = stringResource(R.string.grid_and_cards),
            items = listOf(
                IosSettingsItem(
                    icon = painterResource(R.drawable.grid_view),
                    title = { Text(stringResource(R.string.pure_black_hero_background)) },
                    description = { Text(stringResource(R.string.pure_black_hero_background_desc)) },
                    trailingContent = {
                        Switch(
                            checked = pureBlackHeroBackground,
                            onCheckedChange = onPureBlackHeroBackgroundChange
                        )
                    },
                    onClick = { onPureBlackHeroBackgroundChange(!pureBlackHeroBackground) }
                ),
                IosSettingsItem(
                    icon = painterResource(R.drawable.grid_view),
                    title = { Text(stringResource(R.string.home_hero_card)) },
                    description = { Text(stringResource(R.string.home_hero_card_desc)) },
                    trailingContent = {
                        Switch(
                            checked = homeHeroCardEnabled,
                            onCheckedChange = onHomeHeroCardEnabledChange,
                        )
                    },
                    onClick = { onHomeHeroCardEnabledChange(!homeHeroCardEnabled) },
                ),
            )
        )

        Spacer(modifier = Modifier.height(27.dp))

        IosSettingsGroup(
            title = stringResource(R.string.auto_playlists),
            items = listOf(
                IosSettingsItem(
                    icon = rememberVectorPainter(SfSymbols.Monochrome.SFStar),
                    title = { Text(stringResource(R.string.show_liked_playlist)) },
                    trailingContent = {
                        Switch(
                            checked = showLikedPlaylist,
                            onCheckedChange = onShowLikedPlaylistChange
                        )
                    },
                    onClick = { onShowLikedPlaylistChange(!showLikedPlaylist) }
                ),
                IosSettingsItem(
                    icon = rememberVectorPainter(SfSymbols.Monochrome.SFIcloudAndArrowDownFill),
                    title = { Text(stringResource(R.string.show_downloaded_playlist)) },
                    trailingContent = {
                        Switch(
                            checked = showDownloadedPlaylist,
                            onCheckedChange = onShowDownloadedPlaylistChange
                        )
                    },
                    onClick = { onShowDownloadedPlaylistChange(!showDownloadedPlaylist) }
                ),
                IosSettingsItem(
                    icon = painterResource(R.drawable.trending_up),
                    title = { Text(stringResource(R.string.show_top_playlist)) },
                    trailingContent = {
                        Switch(
                            checked = showTopPlaylist,
                            onCheckedChange = onShowTopPlaylistChange
                        )
                    },
                    onClick = { onShowTopPlaylistChange(!showTopPlaylist) }
                ),
                IosSettingsItem(
                    icon = painterResource(R.drawable.cached),
                    title = { Text(stringResource(R.string.show_cached_playlist)) },
                    trailingContent = {
                        Switch(
                            checked = showCachedPlaylist,
                            onCheckedChange = onShowCachedPlaylistChange
                        )
                    },
                    onClick = { onShowCachedPlaylistChange(!showCachedPlaylist) }
                ),
                IosSettingsItem(
                    icon = painterResource(R.drawable.backup),
                    title = { Text(stringResource(R.string.show_uploaded_playlist)) },
                    trailingContent = {
                        Switch(
                            checked = showUploadedPlaylist,
                            onCheckedChange = onShowUploadedPlaylistChange
                        )
                    },
                    onClick = { onShowUploadedPlaylistChange(!showUploadedPlaylist) }
                )
            )
        )

        Spacer(modifier = Modifier.height(27.dp))

        IosSettingsGroup(
            title = stringResource(R.string.advanced),
            items = listOf(
                IosSettingsItem(
                    icon = painterResource(R.drawable.tune),
                    title = { Text(stringResource(R.string.advanced)) },
                    description = { Text(stringResource(R.string.advanced_settings_desc)) },
                    onClick = { navController.navigate("settings/appearance/advanced") }
                )
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
            }
        }
    
    }
}

enum class DarkMode {
    ON,
    OFF,
    AUTO,
}

enum class NavigationTab {
    HOME,
    SEARCH,
    LIBRARY,
}

enum class LyricsPosition {
    LEFT,
    CENTER,
    RIGHT,
}

enum class PlayerTextAlignment {
    SIDED,
    CENTER,
}
