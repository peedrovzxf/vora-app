package com.peedrovzxf.vora.ui.library.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.player.PlayerController

@Composable
fun SongsTab(songs: List<Song>, playerController: PlayerController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(songs) { song ->
            SongItem(song = song, playerController = playerController, songs = songs)
        }
    }
}

@Composable
fun SongItem(song: Song, playerController: PlayerController, songs: List<Song>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { playerController.play(song, songs) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}