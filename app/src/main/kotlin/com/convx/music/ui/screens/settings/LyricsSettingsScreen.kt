/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.convx.music.R
import com.convx.music.constants.AppleMusicLyricsBlurKey
import com.convx.music.constants.LyricsGlowEffectKey
import com.convx.music.constants.LyricsLineSpacingKey
import com.convx.music.constants.LyricsScrollKey
import com.convx.music.constants.LyricsStandardBlurKey
import com.convx.music.constants.LyricsTextPositionKey
import com.convx.music.ui.component.EnumDialog
import com.convx.music.ui.component.IosSettingsGroup
import com.convx.music.ui.component.IosSettingsItem
import com.convx.music.ui.ios.compat.AppTheme
import com.convx.music.ui.ios.compat.OutlinedTextField
import com.convx.music.ui.ios.compat.Slider
import com.convx.music.ui.ios.compat.Switch
import com.convx.music.ui.ios.compat.Text
import com.convx.music.utils.rememberEnumPreference
import com.convx.music.utils.rememberPreference

/** Unified Lyrics settings: typography, layout, glow, and blur. */
@Composable
fun LyricsSettingsScreen(navController: NavController) {
    val (glow, onGlowChange) = rememberPreference(LyricsGlowEffectKey, defaultValue = true)
    val (appleBlur, onAppleBlurChange) = rememberPreference(AppleMusicLyricsBlurKey, defaultValue = true)
    val (standardBlur, onStandardBlurChange) = rememberPreference(LyricsStandardBlurKey, defaultValue = true)
    val (autoScroll, onAutoScrollChange) = rememberPreference(LyricsScrollKey, defaultValue = true)
    val (lineSpacing, onLineSpacingChange) = rememberPreference(LyricsLineSpacingKey, defaultValue = 1.3f)
    val (lyricsPosition, onLyricsPositionChange) = rememberEnumPreference(LyricsTextPositionKey, defaultValue = LyricsPosition.LEFT)

    var showLyricsPositionDialog by rememberSaveable { mutableStateOf(false) }

    if (showLyricsPositionDialog) {
        EnumDialog(
            onDismiss = { showLyricsPositionDialog = false },
            onSelect = {
                onLyricsPositionChange(it)
                showLyricsPositionDialog = false
            },
            title = stringResource(R.string.lyrics_text_position),
            current = lyricsPosition,
            values = listOf(LyricsPosition.LEFT, LyricsPosition.CENTER),
            valueText = { pos ->
                when (pos) {
                    LyricsPosition.LEFT -> stringResource(R.string.left)
                    LyricsPosition.CENTER -> stringResource(R.string.center)
                    LyricsPosition.RIGHT -> stringResource(R.string.right)
                }
            }
        )
    }

    SettingsPage(navController = navController, title = "Lyrics & display", horizontalPadding = 16.dp) {
        LyricsFontSettings()

        IosSettingsGroup(
            title = "Layout & typography",
            items = listOf(
                IosSettingsItem(
                    icon = painterResource(R.drawable.lyrics),
                    title = { Text(stringResource(R.string.lyrics_text_position)) },
                    description = {
                        Text(
                            when (lyricsPosition) {
                                LyricsPosition.LEFT -> stringResource(R.string.left)
                                LyricsPosition.CENTER -> stringResource(R.string.center)
                                LyricsPosition.RIGHT -> stringResource(R.string.right)
                            }
                        )
                    },
                    onClick = { showLyricsPositionDialog = true },
                ),
                IosSettingsItem(
                    icon = painterResource(R.drawable.lyrics),
                    title = { Text(stringResource(R.string.lyrics_auto_scroll)) },
                    description = { Text("Smoothly follows along with music playback") },
                    trailingContent = { Switch(checked = autoScroll, onCheckedChange = onAutoScrollChange) },
                    onClick = { onAutoScrollChange(!autoScroll) },
                ),
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Line spacing", style = AppTheme.typography.bodyMedium)
                Text(String.format("%.1fx", lineSpacing), style = AppTheme.typography.bodyMedium)
            }
            Slider(
                value = lineSpacing,
                onValueChange = onLineSpacingChange,
                valueRange = 1.0f..1.8f,
            )
        }

        IosSettingsGroup(
            title = "Apple Music effects",
            items = listOf(
                IosSettingsItem(
                    icon = painterResource(R.drawable.lyrics),
                    title = { Text(stringResource(R.string.lyrics_glow_effect)) },
                    description = { Text("Word-level subtle illumination as lyrics play") },
                    trailingContent = { Switch(checked = glow, onCheckedChange = onGlowChange) },
                    onClick = { onGlowChange(!glow) },
                ),
                IosSettingsItem(
                    icon = painterResource(R.drawable.lyrics),
                    title = { Text(stringResource(R.string.apple_music_lyrics_blur)) },
                    description = { Text("Gentle depth-of-field blur on surrounding lines") },
                    trailingContent = { Switch(checked = appleBlur, onCheckedChange = onAppleBlurChange) },
                    onClick = { onAppleBlurChange(!appleBlur) },
                ),
                IosSettingsItem(
                    icon = painterResource(R.drawable.lyrics),
                    title = { Text(stringResource(R.string.standard_lyrics_blur)) },
                    description = { Text("Blur distant lines by distance from active line") },
                    trailingContent = { Switch(checked = standardBlur, onCheckedChange = onStandardBlurChange) },
                    onClick = { onStandardBlurChange(!standardBlur) },
                ),
            ),
        )



        Spacer(Modifier.height(32.dp))
    }
}
