package com.peedrovzxf.vora.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peedrovzxf.vora.data.model.Playlist
import com.peedrovzxf.vora.player.PlayerController
import com.peedrovzxf.vora.ui.liked.LikedSongsViewModel

@Composable
fun PlaylistScreen(
    viewModel        : PlaylistViewModel,
    playerController : PlayerController,
    onOpenPlaylist   : (playlistId: Long, playlistName: String) -> Unit,
    onOpenLikedSongs : () -> Unit = {},
    likedViewModel   : LikedSongsViewModel = viewModel()
) {
    val playlists        by viewModel.playlists.collectAsState()
    val likedCount       by likedViewModel.likedCount.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { showCreateDialog = true },
                shape          = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector        = Icons.Filled.Add,
                    contentDescription = "Create playlist",
                    modifier           = Modifier.size(24.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            item {
                LikedSongsCard(
                    count   = likedCount,
                    onClick = onOpenLikedSongs
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (playlists.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier         = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector        = Icons.AutoMirrored.Filled.QueueMusic,
                                    contentDescription = null,
                                    modifier           = Modifier.size(32.dp),
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            Text(
                                text  = "No playlists yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text  = "Tap + to create your first playlist",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistItem(
                        playlist = playlist,
                        onOpen   = { onOpenPlaylist(playlist.id, playlist.name) },
                        onDelete = { viewModel.deletePlaylist(playlist.id, playlist.name) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onConfirm = { name ->
                viewModel.createPlaylist(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
private fun LikedSongsCard(
    count   : Int,
    onClick : () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    )
                )
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier         = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "Liked Songs",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text  = "$count ${if (count == 1) "song" else "songs"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PlaylistItem(
    playlist : Playlist,
    onOpen   : () -> Unit,
    onDelete : () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Filled.MusicNote,
                contentDescription = null,
                modifier           = Modifier.size(22.dp),
                tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.80f)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = playlist.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text  = "${playlist.songs.size} ${if (playlist.songs.size == 1) "song" else "songs"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick  = { showConfirm = true },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector        = Icons.Filled.Delete,
                contentDescription = "Delete playlist",
                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier           = Modifier.size(20.dp)
            )
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title            = { Text("Delete playlist") },
            text             = {
                Text(
                    text  = "Delete \"${playlist.name}\"? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton    = {
                TextButton(
                    onClick = {
                        onDelete()
                        showConfirm = false
                    }
                ) {
                    Text(
                        text  = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton    = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel")
                }
            },
            shape            = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun CreatePlaylistDialog(
    onConfirm : (String) -> Unit,
    onDismiss : () -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title            = {
            Text(
                text  = "New playlist",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        text             = {
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it },
                label         = { Text("Name") },
                placeholder   = { Text("My playlist") },
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                modifier      = Modifier.fillMaxWidth()
            )
        },
        confirmButton    = {
            TextButton(
                onClick  = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled  = name.isNotBlank()
            ) {
                Text(
                    text       = "Create",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton    = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape            = RoundedCornerShape(20.dp)
    )
}
