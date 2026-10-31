package com.convx.music.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.convx.music.R

data class SearchableSetting(
    val title: String,
    val description: String?,
    val category: String,
    val route: String,
    val keywords: String = "",
)

@Composable
fun getAllSearchableSettings(): List<SearchableSetting> {
    return listOf(
        SearchableSetting(stringResource(R.string.appearance), null, "Appearance", "settings/appearance"),
        SearchableSetting(stringResource(R.string.player_and_audio), null, "Playback & audio", "settings/player"),
        SearchableSetting(stringResource(R.string.theme), null, "Appearance", "settings/appearance/theme"),
        SearchableSetting("App Font", null, "Appearance", "settings/appearance/font"),
        SearchableSetting("Lyrics & display", "Configure lyrics font, size, spacing, glow, blur and display options", "Player & Lyrics", "settings/lyrics", keywords = "lyrics font typeface size text glow blur spacing"),
        SearchableSetting("Player Theme", null, "Appearance", "settings/appearance/playertheme"),
        SearchableSetting("Liquid Glass", null, "Appearance", "settings/appearance/liquidglass"),
        SearchableSetting("Liquid Glass advanced controls", "Glass style, tint, highlights, lens and component settings", "Appearance", "settings/appearance/liquidglass/advanced"),
        SearchableSetting("Hide status bar and navigation buttons", "Immersive fullscreen mode; swipe from edge to show briefly", "Appearance", "settings/appearance", keywords = "fullscreen immersive status bar navigation buttons"),
        SearchableSetting("Navidrome Server", "Connect and stream from Navidrome & Subsonic servers", "Library & storage", "settings/network_storage", keywords = "smb webdav navidrome nas network folder server share subsonic"),
        SearchableSetting("Player design", "Choose Card, Vinyl, Clover, Apple Music with live previews", "Player & Lyrics", "settings/appearance/playertheme"),
        SearchableSetting("Seek bar style", "Progress slider look in the player", "Player & Lyrics", "settings/appearance/playertheme"),
        SearchableSetting("Show next track", "Show Up Next in the player", "Player & Lyrics", "settings/appearance/playertheme"),
        SearchableSetting("Player background", "Blur, glass, live, static color or gradient", "Player & Lyrics", "settings/appearance/playertheme"),
        SearchableSetting("Floating mini player", null, "Controls & layout", "settings/appearance/advanced"),
        SearchableSetting("Bottom navigation bar", null, "Controls & layout", "settings/appearance/advanced"),
        SearchableSetting("Track information badges", null, "Controls & layout", "settings/appearance/advanced"),
        SearchableSetting("Network & storage", null, "Storage & network", "settings/storage"),
        SearchableSetting("Clear cache", null, "Storage & network", "settings/storage"),
        SearchableSetting("Sleep timer", null, "Playback & audio", "settings/player"),
        SearchableSetting("Audio offload", null, "Playback & audio", "settings/player"),
        SearchableSetting("Persistent queue", null, "Playback & audio", "settings/player"),
    )
}
