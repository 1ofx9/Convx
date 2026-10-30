package com.convx.music.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.convx.music.R

data class SearchableSetting(
    val title: String,
    val description: String?,
    val category: String,
    val route: String,
    val opensLyricsSheet: Boolean = false,
    val keywords: String = "",
)

/** One-shot request for the Appearance screen to open the lyrics animation sheet on arrival. */
object SettingsDeepLink {
    private var lyricsSheet = false

    fun requestLyricsSheet() {
        lyricsSheet = true
    }

    fun consumeLyricsSheet(): Boolean = lyricsSheet.also { lyricsSheet = false }
}

@Composable
fun getAllSearchableSettings(): List<SearchableSetting> {
    return listOf(
        SearchableSetting(stringResource(R.string.appearance), null, "Appearance", "settings/appearance"),
        SearchableSetting(stringResource(R.string.player_and_audio), null, "Playback & audio", "settings/player"),
        SearchableSetting(stringResource(R.string.theme), null, "Appearance", "settings/appearance/theme"),
        SearchableSetting("App Font", null, "Appearance", "settings/appearance/font"),
        SearchableSetting("Canvas", null, "Appearance", "settings/appearance/canvas"),
        SearchableSetting("Convx Lyrics effects", "Configure Convx Lyrics font, text size, glow and blur", "Lyrics", "settings/lyrics", keywords = "convx lyrics animation font typeface size text glow blur"),
        SearchableSetting("Player Theme", null, "Appearance", "settings/appearance/playertheme"),
        SearchableSetting("Liquid Glass", null, "Appearance", "settings/appearance/liquidglass"),
        SearchableSetting("Liquid Glass advanced controls", "Glass style, tint, highlights, lens and component settings", "Appearance", "settings/appearance/liquidglass/advanced"),
        SearchableSetting(stringResource(R.string.app_icon), null, "Appearance", "settings/appearance/appicon"),
        SearchableSetting(stringResource(R.string.presets), null, "Appearance", "settings/appearance/presets"),
        SearchableSetting(stringResource(R.string.player_icons), null, "Appearance", "settings/appearance/playericons"),
        SearchableSetting(stringResource(R.string.diy), null, "Appearance", "settings/appearance/diy"),
        SearchableSetting("Scan local music", "Find and refresh songs stored on this device", "Library & storage", "settings/scan_music", keywords = "scan rescan refresh index local files songs"),
        SearchableSetting("Music folders", "Choose local folders to scan and browse", "Library & storage", "settings/player/local_folders", keywords = "folder directory local storage device"),
        SearchableSetting("Navidrome Server", "Connect and stream from Navidrome & Subsonic servers", "Library & storage", "settings/network_storage", keywords = "smb webdav navidrome nas network folder server share subsonic"),
        SearchableSetting(stringResource(R.string.lyrics_provider_priority), null, "Lyrics", "settings/content/advanced"),
        SearchableSetting(stringResource(R.string.lyrics_romanization), null, "Lyrics", "settings/content/romanization"),
        SearchableSetting(stringResource(R.string.lyrics_animation_style), "Preview and choose how lyrics move while a song plays: Apple Music, karaoke, glow", "Lyrics", "settings/appearance", opensLyricsSheet = true),
        SearchableSetting(stringResource(R.string.lyrics_text_position), "Align lyrics left, center, or right", "Lyrics", "settings/appearance", opensLyricsSheet = true),
        SearchableSetting(stringResource(R.string.lyrics_auto_scroll), "Keep the current lyric in view", "Lyrics", "settings/appearance", opensLyricsSheet = true),
        SearchableSetting(stringResource(R.string.lyrics_swipe_to_change_song), null, "Lyrics", "settings/appearance", opensLyricsSheet = true),
        SearchableSetting("Player design", "Choose Card, Vinyl, Clover, Apple Music with live previews", "Player", "settings/appearance/playertheme"),
        SearchableSetting("Seek bar style", "Progress slider look in the player", "Player", "settings/appearance/playertheme"),
        SearchableSetting("Show next track", "Show Up Next in the player", "Player", "settings/appearance/playertheme"),
        SearchableSetting("Player background", "Blur, glass, live, static color or gradient", "Player", "settings/appearance/playertheme"),
        SearchableSetting("Artwork shape", "Square, vinyl or clover cover", "Player", "settings/appearance/playertheme"),
        SearchableSetting(stringResource(R.string.one_tap_fullscreen_lyrics), null, "Playback & audio", "settings/appearance"),
        SearchableSetting(stringResource(R.string.audio_normalization), null, "Playback & audio", "settings/player"),
        SearchableSetting(stringResource(R.string.audio_offload), null, "Playback & audio", "settings/player/advanced"),
        SearchableSetting(stringResource(R.string.persistent_queue), null, "Playback & audio", "settings/player"),
        SearchableSetting(stringResource(R.string.pause_music_when_media_is_muted), null, "Playback & audio", "settings/player"),
        SearchableSetting(stringResource(R.string.resume_on_bluetooth_connect), null, "Playback & audio", "settings/player"),
        SearchableSetting(stringResource(R.string.stop_music_on_task_clear), null, "Playback & audio", "settings/player/advanced"),
        SearchableSetting(stringResource(R.string.keep_screen_on_when_player_is_expanded), null, "Playback & audio", "settings/player"),
        SearchableSetting(stringResource(R.string.history_duration), null, "Playback & audio", "settings/player/advanced"),
        SearchableSetting(stringResource(R.string.skip_silence), null, "Playback & audio", "settings/player"),
        SearchableSetting("Lyrics glow", "Glow behind the current lyric line", "Lyrics", "settings/lyrics"),
        SearchableSetting(stringResource(R.string.apple_music_lyrics_blur), "Blur lyric lines away from the current one", "Lyrics", "settings/lyrics"),
        SearchableSetting(stringResource(R.string.standard_lyrics_blur), "Blur for standard lyric styles", "Lyrics", "settings/lyrics"),
        SearchableSetting(stringResource(R.string.local_only_mode), "Only play songs stored on this device", "Settings", "settings"),
        SearchableSetting(stringResource(R.string.hide_volume_bar), "Show or hide the volume slider in the player", "Player", "settings/appearance/playertheme"),
        SearchableSetting(stringResource(R.string.crop_album_art), null, "Appearance", "settings/appearance"),
        SearchableSetting(stringResource(R.string.display_density), null, "Appearance", "settings/appearance/advanced"),
        SearchableSetting(stringResource(R.string.grid_card_height), null, "Appearance", "settings/appearance/advanced"),
        SearchableSetting(stringResource(R.string.privacy), null, "Privacy & storage", "settings/privacy"),
        SearchableSetting(stringResource(R.string.pause_listen_history), null, "Privacy & storage", "settings/privacy"),
        SearchableSetting(stringResource(R.string.pause_search_history), null, "Privacy & storage", "settings/privacy"),
        SearchableSetting(stringResource(R.string.clear_listen_history), null, "Privacy & storage", "settings/privacy"),
        SearchableSetting(stringResource(R.string.clear_search_history), null, "Privacy & storage", "settings/privacy"),
        SearchableSetting(stringResource(R.string.disable_screenshot), null, "Privacy & storage", "settings/privacy"),
        SearchableSetting(stringResource(R.string.storage), null, "Privacy & storage", "settings/storage"),
        SearchableSetting(stringResource(R.string.image_cache), null, "Privacy & storage", "settings/storage"),
        SearchableSetting(stringResource(R.string.song_cache), null, "Privacy & storage", "settings/storage"),
        SearchableSetting(stringResource(R.string.clear_image_cache), null, "Privacy & storage", "settings/storage"),
        SearchableSetting(stringResource(R.string.clear_song_cache), null, "Privacy & storage", "settings/storage"),
        SearchableSetting(stringResource(R.string.downloaded_songs), null, "Privacy & storage", "settings/storage"),
        SearchableSetting(stringResource(R.string.clear_all_downloads), null, "Privacy & storage", "settings/storage"),
        SearchableSetting(stringResource(R.string.backup_restore), null, "Privacy & storage", "settings/backup_restore"),
        SearchableSetting("Import from local file", null, "Privacy & storage", "settings/backup_restore"),
        SearchableSetting(stringResource(R.string.history), null, "Your listening", "history"),
        SearchableSetting(stringResource(R.string.stats), null, "Your listening", "stats"),
    )
}
