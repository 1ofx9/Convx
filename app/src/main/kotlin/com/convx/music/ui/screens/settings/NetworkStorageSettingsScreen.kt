/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.convx.music.R
import com.convx.music.ui.component.SymbolIcon
import com.convx.music.ui.component.sf
import com.convx.music.ui.ios.compat.AlertDialog
import com.convx.music.ui.ios.compat.AppTheme
import com.convx.music.ui.ios.compat.Button
import com.convx.music.ui.ios.compat.CircularProgressIndicator
import com.convx.music.ui.ios.compat.HorizontalDivider
import com.convx.music.ui.ios.compat.Icon
import com.convx.music.ui.ios.compat.OutlinedButton
import com.convx.music.ui.ios.compat.OutlinedTextField
import com.convx.music.ui.ios.compat.Surface
import com.convx.music.ui.ios.compat.Text
import com.convx.music.ui.ios.compat.TextButton
import com.convx.music.viewmodels.NetworkStorageViewModel
import com.convx.storage.domain.NetworkProtocol
import com.convx.storage.domain.NetworkServer

/** Navidrome and self-hosted music sources: add a server, test it, and scan its audio into the library. */
@Composable
fun NetworkStorageSettingsScreen(
    navController: NavController,
    @Suppress("UNUSED_PARAMETER") scrollBehavior: TopAppBarScrollBehavior,
) {
    val viewModel: NetworkStorageViewModel = hiltViewModel()
    val servers by viewModel.servers.collectAsStateWithLifecycle()
    val busyId by viewModel.busyId.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<NetworkServer?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    SettingsPage(
        navController = navController,
        title = "Navidrome & Servers",
        pageColor = AppTheme.colorScheme.background,
        horizontalPadding = 20.dp,
    ) {
        Spacer(Modifier.height(4.dp))

        Button(
            onClick = { showAdd = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        ) {
            SymbolIcon(symbol = sf("plus"), contentDescription = null, size = 18.dp)
            Spacer(Modifier.width(8.dp))
            Text("Add Server", fontWeight = FontWeight.SemiBold)
        }

        AnimatedVisibility(
            visible = status != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            status?.let { msg ->
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AppTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SymbolIcon(symbol = sf("info.circle"), contentDescription = null, size = 18.dp, tint = AppTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = msg,
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "CONFIGURED SERVERS",
            style = AppTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )

        if (servers.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.storage),
                        contentDescription = null,
                        tint = AppTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No Servers Configured",
                        style = AppTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Add your Navidrome, WebDAV, or SMB server above to begin streaming.",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                servers.forEach { server ->
                    ServerCard(
                        server = server,
                        busy = busyId == server.id,
                        enabled = busyId == null,
                        onTest = { viewModel.test(server) },
                        onScan = { viewModel.scan(server) },
                        onDelete = { deleting = server },
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showAdd) {
        AddServerDialog(
            onDismiss = { showAdd = false },
            onSave = { protocol, name, address, user, password, domain ->
                val cleanAddress = address.trim().let { if (it.contains("://")) it else "http://$it" }
                viewModel.add(protocol, name.trim(), cleanAddress, user.trim(), password, domain.trim())
                showAdd = false
            },
        )
    }

    deleting?.let { server ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.network_storage_remove_title, server.name)) },
            text = { Text("Are you sure you want to remove this server? This will not delete any files on the remote server.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.delete(server); deleting = null },
                ) {
                    Text(stringResource(android.R.string.ok), color = AppTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text(stringResource(android.R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun ServerCard(
    server: NetworkServer,
    busy: Boolean,
    enabled: Boolean,
    onTest: () -> Unit,
    onScan: () -> Unit,
    onDelete: () -> Unit,
) {
    val protocolLabel = when (server.protocol) {
        NetworkProtocol.NAVIDROME -> "Navidrome (Subsonic)"
        NetworkProtocol.WEBDAV -> "WebDAV"
        NetworkProtocol.SMB -> "SMB / CIFS"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AppTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF34C759).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cloud),
                        contentDescription = null,
                        tint = Color(0xFF34C759),
                        modifier = Modifier.size(22.dp),
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = server.name,
                            style = AppTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = AppTheme.colorScheme.primary.copy(alpha = 0.12f),
                        ) {
                            Text(
                                text = "Lossless",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = AppTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colorScheme.primary,
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "$protocolLabel • ${server.address}",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }

                if (busy) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = AppTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onScan,
                    enabled = enabled,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    SymbolIcon(symbol = sf("arrow.triangle.2.circlepath"), contentDescription = null, size = 16.dp)
                    Spacer(Modifier.width(6.dp))
                    Text("Sync Library", style = AppTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onTest,
                    enabled = enabled,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    SymbolIcon(symbol = sf("bolt.fill"), contentDescription = null, size = 14.dp)
                    Spacer(Modifier.width(6.dp))
                    Text("Test", style = AppTheme.typography.labelMedium)
                }

                TextButton(
                    onClick = onDelete,
                    enabled = enabled,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    SymbolIcon(symbol = sf("trash"), contentDescription = null, size = 16.dp, tint = AppTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun AddServerDialog(
    onDismiss: () -> Unit,
    onSave: (NetworkProtocol, String, String, String, String, String) -> Unit,
) {
    var protocol by remember { mutableStateOf(NetworkProtocol.NAVIDROME) }
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("") }

    val protocols = listOf(
        NetworkProtocol.NAVIDROME to "Navidrome",
        NetworkProtocol.WEBDAV to "WebDAV",
        NetworkProtocol.SMB to "SMB",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (protocol == NetworkProtocol.NAVIDROME) "Add Navidrome Server" else "Add Music Server",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    protocols.forEach { (p, label) ->
                        if (protocol == p) {
                            Button(
                                onClick = {},
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            ) {
                                Text(label, style = AppTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { protocol = p },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            ) {
                                Text(label, style = AppTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Server Name") },
                    placeholder = { Text("e.g. My Navidrome") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = {
                        Text(
                            when (protocol) {
                                NetworkProtocol.NAVIDROME -> "Server URL"
                                NetworkProtocol.WEBDAV -> "WebDAV URL"
                                NetworkProtocol.SMB -> "SMB Host"
                            },
                        )
                    },
                    placeholder = {
                        Text(
                            when (protocol) {
                                NetworkProtocol.NAVIDROME -> "http://192.168.1.100:4533"
                                NetworkProtocol.WEBDAV -> "https://webdav.myhost.com"
                                NetworkProtocol.SMB -> "smb://192.168.1.100/music"
                            },
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = user,
                    onValueChange = { user = it },
                    label = { Text("Username") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                )
                if (protocol == NetworkProtocol.SMB) {
                    OutlinedTextField(
                        value = domain,
                        onValueChange = { domain = it },
                        label = { Text("Domain / Workgroup (Optional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(protocol, name, address, user, password, domain) },
                enabled = name.isNotBlank() && address.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
            ) { Text("Save Server") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
