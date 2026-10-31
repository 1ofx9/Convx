/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.convx.music.R
import com.convx.music.ui.component.IosPageSheet
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.convx.music.ui.component.SymbolIcon
import com.convx.music.ui.component.sf
import com.convx.music.ui.component.shapes.ContinuousRoundedRectangle
import com.convx.music.ui.ios.compat.HorizontalDivider
import com.convx.music.ui.ios.compat.Icon
import com.convx.music.ui.ios.compat.Text
import com.convx.music.ui.ios.foundation.IosColors
import com.convx.music.ui.ios.foundation.IosMetrics
import com.convx.music.ui.ios.foundation.IosTheme
import com.convx.music.ui.ios.foundation.iosRowPress

/** What a settings row does when tapped. */
sealed interface SettingsAction {
    data class Route(val route: String) : SettingsAction
    data object PlayerDesign : SettingsAction
}

/** One row: a coloured glyph tile, a name, and where it goes. */
data class SettingsLink(
    val title: String,
    val icon: Int,
    val tint: IosColors.() -> Color,
    val action: SettingsAction,
    val subtitle: String? = null,
    /** Extra words people search with that are not in the title. */
    val keywords: String = "",
)

data class SettingsLinkGroup(val title: String?, val links: List<SettingsLink>)

/** A root category and the page it opens. */
data class SettingsHub(
    val id: String,
    val title: String,
    val icon: Int,
    val tint: IosColors.() -> Color,
    val groups: List<SettingsLinkGroup>,
) {
    val route get() = "settings/hub/$id"
    fun asLink() = SettingsLink(title, icon, tint, SettingsAction.Route(route))
}

private fun route(r: String) = SettingsAction.Route(r)

/** The settings tree: root categories, the pages they open, and every destination under them. */
object SettingsHubs {
    val player = SettingsHub(
        "player", "Player & Lyrics", R.drawable.play, { systemPink },
        listOf(
            SettingsLinkGroup(
                "Player",
                listOf(
                    SettingsLink("Player design", R.drawable.play, { systemPink }, SettingsAction.PlayerDesign,
                        "Card, Vinyl, Clover, Apple Music — live previews", "theme layout background seek bar slider up next shadow volume bar"),
                    SettingsLink("Controls & layout", R.drawable.sliders, { systemIndigo }, route("settings/appearance/advanced"),
                        "Thumbnail, mini player, buttons and badges", "mini player audio quality badge thumbnail"),
                ),
            ),
            SettingsLinkGroup(
                "Lyrics",
                listOf(
                    SettingsLink("Lyrics & display", R.drawable.lyrics, { systemPink }, route("settings/lyrics"),
                        "Font, size, glow, blur and line spacing", "font size glow blur display text spacing alignment"),
                ),
            ),
        ),
    )

    val playback = SettingsHub(
        "playback", "Playback & Audio", R.drawable.equalizer, { systemRed },
        listOf(
            SettingsLinkGroup(
                "Sound & Rules",
                listOf(
                    SettingsLink("Audio rules", R.drawable.tune, { systemBlue }, route("settings/player"),
                        "Bit-perfect streaming, normalization, and Bluetooth rules", "quality normalization loudness silence bit perfect bluetooth output"),
                ),
            ),
            SettingsLinkGroup(
                "Queue & Behavior",
                listOf(
                    SettingsLink("Queue & playback behavior", R.drawable.queue_music, { systemOrange }, route("settings/player/advanced"),
                        "Persistent shuffle, duplicate tracks, and queue loading", "persistent queue autoplay shuffle repeat duplicates offload"),
                ),
            ),
        ),
    )

    val appearance = SettingsHub(
        "appearance", "Appearance", R.drawable.palette, { systemPurple },
        listOf(
            SettingsLinkGroup(
                "Theme & colors",
                listOf(
                    SettingsLink("Theme", R.drawable.palette, { systemPurple }, route("settings/appearance/theme"), "Accent, dark mode and pure black", "dark light accent color"),
                    SettingsLink("Liquid Glass", R.drawable.palette, { systemCyan }, route("settings/appearance/liquidglass"), "Blur, tint, highlights and lens", "glass blur"),
                ),
            ),
            SettingsLinkGroup(
                "App",
                listOf(
                    SettingsLink("Font", R.drawable.alphabet_cyrillic, { systemGray }, route("settings/appearance/font"), "App typeface"),
                    SettingsLink("Layout & home", R.drawable.sliders, { systemGreen }, route("settings/appearance"),
                        "Language, default tab, density, home", "language default tab density refresh rate"),
                    SettingsLink("Home feed order", R.drawable.sliders, { systemOrange }, route("settings/home_feed_order"), "Arrange home sections"),
                ),
            ),
            SettingsLinkGroup(
                "Advanced",
                listOf(
                    SettingsLink("Advanced appearance", R.drawable.settings, { systemGray }, route("settings/appearance/advanced"),
                        "Grids, corners, blur and overscroll", "grid corner radius blur overscroll pure black"),
                ),
            ),
        ),
    )

    val library = SettingsHub(
        "library", "Server & Storage", R.drawable.storage, { systemOrange },
        listOf(
            SettingsLinkGroup(
                "Music Sources",
                listOf(
                    SettingsLink("Navidrome Server", R.drawable.storage, { systemGreen }, route("settings/network_storage"), "Connect Navidrome & Subsonic sources", "navidrome subsonic server lossless"),
                ),
            ),
            SettingsLinkGroup(
                "Storage & Backup",
                listOf(
                    SettingsLink("Storage", R.drawable.storage, { systemGray }, route("settings/storage"), "Downloads and caches", "cache downloads clear"),
                ),
            ),
        ),
    )

    val privacy = SettingsHub(
        "privacy", "Privacy & Security", R.drawable.security, { systemBlue },
        listOf(
            SettingsLinkGroup(
                null,
                listOf(
                    SettingsLink("Privacy", R.drawable.security, { systemBlue }, route("settings/privacy"), "History and playback privacy", "pause history clear history screenshot"),
                ),
            ),
        ),
    )

    val all = listOf(player, playback, appearance, library, privacy)

    fun byId(id: String?) = all.firstOrNull { it.id == id }
}

/** Which settings sheet is open; one app-wide slot so no two screens can each open their own. */
object SettingsSheets {
    var open by mutableStateOf<SettingsAction?>(null)
}

/** Runs a row's action: a page, or one of the sheets that open over the current screen. */
@Composable
fun rememberSettingsActionHandler(navController: NavController): (SettingsAction) -> Unit = { action ->
    when (action) {
        is SettingsAction.Route -> navController.navigate(action.route)
        SettingsAction.PlayerDesign -> SettingsSheets.open = action
    }
}

/** Hosted once at the app root; shows whichever settings sheet is open. */
@Composable
fun SettingsSheetHost(navController: NavController) {
    val close = { SettingsSheets.open = null }
    when (SettingsSheets.open) {
        SettingsAction.PlayerDesign -> IosPageSheet("Player design", close) {
            PlayerDesignSheetContent(onMoreOptions = {
                close()
                navController.navigate("settings/appearance/playertheme")
            })
        }
        else -> Unit
    }
}

/** A category's page: its groups of rows, iOS inset-grouped. */
@Composable
fun SettingsHubScreen(navController: NavController, hub: SettingsHub) {
    val onAction = rememberSettingsActionHandler(navController)
    SettingsPage(navController = navController, title = hub.title, horizontalPadding = 16.dp) {
        SettingsHubBody(hub, onAction)
    }
}

@Composable
fun ColumnScope.SettingsHubBody(hub: SettingsHub, onAction: (SettingsAction) -> Unit) {
    hub.groups.forEachIndexed { index, group ->
        if (index > 0) Spacer(Modifier.height(28.dp))
        SettingsLinkCard(group.title, group.links, onAction)
    }
}

@Composable
fun SettingsLinkCard(
    title: String?,
    links: List<SettingsLink>,
    onAction: (SettingsAction) -> Unit,
    trailing: (SettingsLink) -> String? = { null },
    selected: SettingsLink? = null,
) {
    Column(Modifier.fillMaxWidth()) {
        if (title != null) Text(
            title,
            style = IosTheme.typography.footnote,
            fontWeight = FontWeight.SemiBold,
            color = IosTheme.colors.secondaryLabel,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .clip(ContinuousRoundedRectangle(IosMetrics.GroupedCornerRadius))
                .background(IosTheme.colors.secondarySystemGroupedBackground),
        ) {
            links.forEachIndexed { i, link ->
                if (i > 0) HorizontalDivider(
                    Modifier.padding(start = 58.dp),
                    color = IosTheme.colors.separator,
                    thickness = IosMetrics.SeparatorThickness,
                )
                SettingsLinkRow(link, trailing(link), selected == link) { onAction(link.action) }
            }
        }
    }
}

@Composable
fun SettingsLinkRow(link: SettingsLink, trailing: String?, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (selected) Modifier.background(IosTheme.colors.systemFill) else Modifier)
            .iosRowPress(onClick = onClick)
            .defaultMinSize(minHeight = 52.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(ContinuousRoundedRectangle(7.dp))
                .background(link.tint(IosTheme.colors)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(link.icon), null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(link.title, style = IosTheme.typography.body, color = IosTheme.colors.label)
            if (link.subtitle != null) Text(
                link.subtitle,
                style = IosTheme.typography.footnote,
                color = IosTheme.colors.secondaryLabel,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (trailing != null) {
            Text(trailing, style = IosTheme.typography.body, color = IosTheme.colors.secondaryLabel, maxLines = 1)
            Spacer(Modifier.width(8.dp))
        }
        SymbolIcon(symbol = sf("chevron.right"), contentDescription = null, size = 14.dp, tint = IosTheme.colors.tertiaryLabel)
    }
}
