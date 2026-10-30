/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.convx.music.constants.ListenTogetherInTopBarKey
import com.convx.music.constants.LocalOnlyModeKey
import com.convx.music.constants.HiddenNavigationRoutesKey
import com.convx.music.ui.screens.Screens
import com.convx.music.utils.rememberPreference

/** Whether the app runs on self-hosted and device files only. Provided once at the root; screens read this instead of the preference. */
val LocalLocalOnlyMode = androidx.compose.runtime.staticCompositionLocalOf { true }

/** Destinations shown by the app's navigation bars. */
@Stable
class AppNavItems(
    val listenTogetherInTopBar: Boolean,
    val localOnlyMode: Boolean,
    val navigationItems: List<Screens>,
    val floatingNavigationItems: List<Screens>,
)

@Composable
fun rememberAppNavItems(): AppNavItems {
    val (listenTogetherInTopBar) = rememberPreference(ListenTogetherInTopBarKey, defaultValue = false)
    // Read once here because both navigation bars use this preference. Defaults to true for Navidrome/Local.
    val (localOnlyMode) = rememberPreference(LocalOnlyModeKey, true)
    val (hiddenNavigationRoutes) = rememberPreference(HiddenNavigationRoutesKey, "")
    val hiddenRoutes = remember(hiddenNavigationRoutes) {
        hiddenNavigationRoutes.lineSequence().map(String::trim).filter(String::isNotEmpty).toSet()
    }
    val navigationItems = remember(listenTogetherInTopBar, localOnlyMode, hiddenRoutes) {
        val screens = Screens.mainScreens(localOnlyMode)
        val visible = screens.filter { it != Screens.ListenTogether && it != Screens.Radio && it != Screens.Explore }
        visible.filter { it == Screens.Search || it.route !in hiddenRoutes }
    }
    val floatingNavigationItems = remember(navigationItems, localOnlyMode, hiddenRoutes) {
        navigationItems
    }
    return remember(listenTogetherInTopBar, localOnlyMode, navigationItems, floatingNavigationItems) {
        AppNavItems(listenTogetherInTopBar, localOnlyMode, navigationItems, floatingNavigationItems)
    }
}
