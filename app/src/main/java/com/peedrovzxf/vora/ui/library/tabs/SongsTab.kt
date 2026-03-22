package com.peedrovzxf.vora.ui.library.tabs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(songs) { song ->
            SongItem(
                song = song,
                playerController = playerController,
                songs = songs,
                playlists = playlists,
                onAddToPlaylist = onAddToPlaylist,
                onEditSong = onEditSong,
                onAddToQueue = onAddToQueue
            )
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongItem(
    song: Song,
    playerController: PlayerController,
    songs: List<Song>,
    playlists: List<Playlist>,
    onAddToPlaylist: (playlistId: Long, songId: Long) -> Unit,
    onEditSong: (songId: Long) -> Unit,
    onAddToQueue: (Song) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { playerController.play(song, songs) },
                    onLongClick = { showMenu = true }
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 12.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            Text(
                text = "Options",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            DropdownMenuItem(
                text = { Text("Edit info") },
                onClick = {
                    onEditSong(song.id)
                    showMenu = false
                }
            )
            HorizontalDivider()

            DropdownMenuItem(
                text = { Text("Add to queue") },
                onClick = {
                    onAddToQueue(song)
                    showMenu = false
                }
            )
            HorizontalDivider()
            Text(
                text = "Add to playlist",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            if (playlists.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No playlists yet") },
                    onClick = { showMenu = false }
                )
            } else {
                playlists.forEach { playlist ->
                    DropdownMenuItem(
                        text = { Text(playlist.name) },
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