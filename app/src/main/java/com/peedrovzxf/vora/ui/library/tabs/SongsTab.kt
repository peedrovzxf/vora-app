package com.peedrovzxf.vora.ui.library.tabs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.peedrovzxf.vora.data.model.Playlist
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.player.PlayerController

@Composable
fun SongsTab(
    songs: List<Song>,
    playerController: PlayerController,
    playlists: List<Playlist>,
    onAddToPlaylist: (playlistId: Long, songId: Long) -> Unit,
    onEditSong: (songId: Long) -> Unit,
    onAddToQueue: (Song) -> Unit
) {
    val currentSong by playerController.currentSong.collectAsState()

    if (songs.isEmpty()) {
        Box(
            modifier         = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Filled.MusicNote,
                    contentDescription = null,
                    modifier           = Modifier.size(48.dp),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Text(
                    text  = "No songs found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
        return
    }

    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(songs, key = { it.id }) { song ->
            SongItem(
                song             = song,
                isCurrentSong    = song.id == currentSong?.id,
                playerController = playerController,
                songs            = songs,
                playlists        = playlists,
                onAddToPlaylist  = onAddToPlaylist,
                onEditSong       = onEditSong,
                onAddToQueue     = onAddToQueue
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongItem(
    song             : Song,
    isCurrentSong    : Boolean,
    playerController : PlayerController,
    songs            : List<Song>,
    playlists        : List<Playlist>,
    onAddToPlaylist  : (playlistId: Long, songId: Long) -> Unit,
    onEditSong       : (songId: Long) -> Unit,
    onAddToQueue     : (Song) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isCurrentSong) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                else androidx.compose.ui.graphics.Color.Transparent
            )
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick     = { playerController.play(song, songs) },
                    onLongClick = { showMenu = true }
                )
                .padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (song.albumArtUri != null) {
                    AsyncImage(
                        model              = song.albumArtUri.let {
                            if (it.startsWith("/")) java.io.File(it) else it
                        },
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape    = RoundedCornerShape(8.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector        = Icons.Filled.MusicNote,
                                contentDescription = null,
                                modifier           = Modifier.size(20.dp),
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = song.title,
                    style    = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isCurrentSong) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color    = if (isCurrentSong)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text     = song.artist,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        DropdownMenu(
            expanded         = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            DropdownMenuItem(
                text    = { Text("Edit info") },
                onClick = {
                    onEditSong(song.id)
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text    = { Text("Add to queue") },
                onClick = {
                    onAddToQueue(song)
                    showMenu = false
                }
            )

            if (playlists.isNotEmpty()) {
                HorizontalDivider()
                DropdownMenuItem(
                    text    = {
                        Text(
                            text  = "Add to playlist",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = {},
                    enabled = false
                )
                playlists.forEach { playlist ->
                    DropdownMenuItem(
                        text    = { Text(playlist.name) },
                        onClick = {
                            onAddToPlaylist(playlist.id, song.id)
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}
