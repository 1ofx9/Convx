/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.component

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.convx.music.App
import com.convx.music.R
import com.convx.music.sync.NetworkSyncManager
import com.convx.music.ui.ios.compat.AppTheme
import com.convx.music.ui.ios.compat.Text
import com.convx.music.ui.ios.foundation.IosTheme
import com.convx.music.ui.screens.library.apple.SfStyle
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.platform.LocalContext

fun localAudioPermission(): String = ""
fun hasLocalAudioPermission(context: Context): Boolean = true

@Composable
fun rememberLocalAudioPermission(onResult: (granted: Boolean) -> Unit): LocalAudioPermission =
    remember { LocalAudioPermission(blocked = false, request = {}, granted = true) }

class LocalAudioPermission(val blocked: Boolean, val request: () -> Unit, val granted: Boolean = true)

@Composable
fun LocalMusicPermissionPrompt(
    permission: LocalAudioPermission,
    modifier: Modifier = Modifier,
) {
    // No-op in network streaming mode
}

/** Shown when library has no audio; handles both unsynced and unconnected server states. */
@Composable
fun LocalMusicEmptyState(
    onScan: (() -> Unit)? = null,
    onAddServer: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val app = context.applicationContext as? App
    val networkRepo = remember(app) { app?.networkRepository?.get() }
    val syncManager = remember(app) { app?.syncManager?.get() }
    val servers by (networkRepo?.servers ?: MutableStateFlow(emptyList())).collectAsStateWithLifecycle()
    val syncState by (syncManager?.syncState ?: MutableStateFlow(NetworkSyncManager.SyncState())).collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        networkRepo?.loadServers()
    }

    if (servers.isEmpty()) {
        PromptLayout(
            icon = R.drawable.storage,
            title = "No Server Connected",
            body = "Connect your Navidrome or Subsonic server to stream your music library.",
            modifier = modifier,
        ) {
            PromptAction("Connect Server") {
                onAddServer?.invoke()
            }
        }
    } else {
        val title = when {
            syncState.isSyncing -> "Syncing Library…"
            syncState.error != null -> "Sync Failed"
            else -> "Library Not Synced"
        }
        val body = when {
            syncState.isSyncing -> syncState.message ?: "Fetching songs from ${servers.firstOrNull()?.name ?: "server"}…"
            syncState.error != null -> syncState.error ?: "Unable to sync with server"
            else -> "Connected to ${servers.firstOrNull()?.name ?: "server"}. Sync your music to start streaming."
        }

        PromptLayout(
            icon = R.drawable.storage,
            title = title,
            body = body,
            modifier = modifier,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PromptAction(
                    label = if (syncState.isSyncing) "Syncing…" else "Sync Library",
                    onClick = {
                        if (!syncState.isSyncing) {
                            syncManager?.sync(force = true)
                            onScan?.invoke()
                        }
                    },
                )

                if (onAddServer != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Manage Servers",
                        style = SfStyle(15.sp, 20.sp, FontWeight.Medium, (-0.23).sp),
                        color = IosTheme.tint,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onAddServer.invoke() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PromptLayout(
    @DrawableRes icon: Int,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(AppTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = AppTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = AppTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        action()
    }
}

@Composable
private fun PromptAction(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colorScheme.primary,
            contentColor = AppTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
    }
}
