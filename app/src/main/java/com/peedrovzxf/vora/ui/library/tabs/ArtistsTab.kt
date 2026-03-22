package com.peedrovzxf.vora.ui.library.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.peedrovzxf.vora.data.model.Artist
import com.peedrovzxf.vora.player.PlayerController

@Composable
fun ArtistsTab(artists: List<Artist>, playerController: PlayerController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(artists) { artist ->
            ArtistItem(artist = artist, playerController = playerController)
        }
    }
}

@Composable
fun ArtistItem(artist: Artist, playerController: PlayerController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { playerController.play(artist.songs.first(), artist.songs) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${artist.albums.size} albums · ${artist.songs.size} songs",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}