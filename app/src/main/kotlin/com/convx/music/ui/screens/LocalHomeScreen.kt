/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.convx.music.LocalPlayerAwareWindowInsets
import com.convx.music.LocalPlayerConnection
import com.convx.music.R
import com.convx.music.constants.HomeGridColumnsOverrideKey
import com.convx.music.constants.ShowHomeFabKey
import com.convx.music.extensions.toMediaItem
import com.convx.music.playback.queues.ListQueue
import com.convx.music.ui.component.HideOnScrollFAB
import com.convx.music.ui.component.HomeImageBackground
import com.convx.music.ui.component.LocalMenuState
import com.convx.music.ui.component.rememberAppBackgroundColor
import com.convx.music.ui.ios.compat.AppTheme
import com.convx.music.ui.theme.AppPage
import com.convx.music.ui.theme.LocalMetadataColor
import com.convx.music.ui.theme.LocalPageMetrics
import com.convx.music.utils.rememberPreference

/**
 * Home for Navidrome streaming mode: the server library under the refined liquid glass chrome.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LocalHomeScreen(
    navController: NavController,
    viewModel: com.convx.music.viewmodels.LocalHomeViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val isPlayingState = playerConnection.isEffectivelyPlaying.collectAsStateWithLifecycle()
    val mediaMetadataState = playerConnection.mediaMetadata.collectAsStateWithLifecycle()

    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val localSongs by viewModel.songs.collectAsStateWithLifecycle()
    val localAlbums by viewModel.albums.collectAsStateWithLifecycle()
    val localArtists by viewModel.artists.collectAsStateWithLifecycle()
    val localPlaylists by viewModel.playlists.collectAsStateWithLifecycle()

    val pullRefreshState = rememberPullToRefreshState()
    val lazyListState = rememberLazyListState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollToTop =
        backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsStateWithLifecycle()
    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            lazyListState.animateScrollToItem(0)
            backStackEntry?.savedStateHandle?.set("scrollToTop", false)
        }
    }

    val (homeGridColumnsOverride) = rememberPreference(HomeGridColumnsOverrideKey, 0)
    val keepListeningColumns = if (homeGridColumnsOverride > 0) homeGridColumnsOverride else 2

    val itemHandlers = rememberHomeItemHandlers(
        navController, playerConnection, menuState, haptic, scope, mediaMetadataState, isPlayingState,
    )

    CompositionLocalProvider(
        LocalPageMetrics provides AppPage.Metrics,
        LocalMetadataColor provides AppTheme.colorScheme.onSurfaceVariant,
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart,
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(rememberAppBackgroundColor(AppTheme.colorScheme.surface)),
            )
            HomeImageBackground(contentLoaded = false)

            val sideInset = LocalPlayerAwareWindowInsets.current
                .asPaddingValues()
                .calculateStartPadding(LocalLayoutDirection.current)

            val deps = HomeSectionDeps(
                context = LocalContext.current,
                randomItem = { null },
                navController = navController,
                playerConnection = playerConnection,
                menuState = menuState,
                haptic = haptic,
                scope = scope,
                sideInset = sideInset,
                localGridItem = itemHandlers.localGridItem,
                onLocalItemClick = itemHandlers.onLocalItemClick,
            )

            PullToRefreshBox(
                isRefreshing = syncState.isSyncing,
                onRefresh = { viewModel.refresh() },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(AppPage.ItemSpacing),
                    contentPadding = run {
                        val ins = LocalPlayerAwareWindowInsets.current.asPaddingValues()
                        val dir = LocalLayoutDirection.current
                        PaddingValues(
                            start = ins.calculateStartPadding(dir),
                            top = maxOf(ins.calculateTopPadding(), if (sideInset > 0.dp) 96.dp else 116.dp),
                            end = ins.calculateRightPadding(dir),
                            bottom = ins.calculateBottomPadding(),
                        )
                    },
                ) {
                    if (localSongs.isEmpty()) {
                        item(key = "local_empty", contentType = "local_access") {
                            com.convx.music.ui.component.LocalMusicEmptyState(
                                onScan = viewModel::refresh,
                                onAddServer = { navController.navigate("settings/network_storage") },
                            )
                        }
                    } else localHomeContent(
                        deps = deps,
                        songs = localSongs,
                        albums = localAlbums,
                        artists = localArtists,
                        playlists = localPlaylists,
                        columns = keepListeningColumns,
                    )

                    item(key = "bottom_spacer", contentType = "spacer") {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }

            val (showHomeFab) = rememberPreference(ShowHomeFabKey, true)
            if (showHomeFab) {
                HideOnScrollFAB(
                    visible = localSongs.isNotEmpty(),
                    lazyListState = lazyListState,
                    icon = R.drawable.shuffle,
                    onClick = {
                        playerConnection.playQueue(
                            ListQueue(items = localSongs.shuffled().map { it.toMediaItem() }, startIndex = 0),
                        )
                    },
                )
            }
        }
    }
}
