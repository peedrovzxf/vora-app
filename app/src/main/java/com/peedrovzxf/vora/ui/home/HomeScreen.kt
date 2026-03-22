package com.peedrovzxf.vora.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.peedrovzxf.vora.data.model.Album
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.player.PlayerController

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    playerController: PlayerController,
    onOpenAlbum: (Long) -> Unit,
    onOpenArtist: (String) -> Unit
) {
    val recentSongs by viewModel.recentSongs.collectAsState()
    val frequentArtists by viewModel.frequentArtists.collectAsState()
    val suggestedAlbums by viewModel.suggestedAlbums.collectAsState()
    val dailySong by viewModel.dailySong.collectAsState()
    val allSongs = viewModel.allSongs.collectAsState().value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Text(
                text = "Vora",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }

        // Canción del día
        dailySong?.let { song ->
            item {
                SectionTitle("Song of the day")
                DailySongCard(
                    song = song,
                    onPlay = { playerController.play(song, allSongs) }
                )
            }
        }

        // Escuchado recientemente
        if (recentSongs.isNotEmpty()) {
            item {
                SectionTitle("Recently played")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recentSongs) { song ->
                        SongCard(
                            song = song,
                            onClick = { playerController.play(song, recentSongs) }
                        )
                    }
                }
            }
        }

        // Artistas frecuentes
        if (frequentArtists.isNotEmpty()) {
            item {
                SectionTitle("Your artists")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(frequentArtists) { artistName ->
                        ArtistChip(
                            name = artistName,
                            onClick = { onOpenArtist(artistName) }
                        )
                    }
                }
            }
        }

        // Álbumes sugeridos
        if (suggestedAlbums.isNotEmpty()) {
            item {
                SectionTitle("Suggested albums")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(suggestedAlbums) { album ->
                        AlbumCard(
                            album = album,
                            onClick = { onOpenAlbum(album.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun DailySongCard(song: Song, onPlay: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onPlay() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.albumArtUri?.let {
                    if (it.startsWith("/")) java.io.File(it) else it
                },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            FilledIconButton(onClick = onPlay) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
            }
        }
    }
}

@Composable
fun SongCard(song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = song.albumArtUri?.let {
                if (it.startsWith("/")) java.io.File(it) else it
            },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AlbumCard(album: Album, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = album.albumArtUri?.let {
                if (it.startsWith("/")) java.io.File(it) else it
            },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = album.name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = album.artist,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ArtistChip(name: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        tonalElevation = 4.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}