package com.peedrovzxf.vora.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val recentSongs     by viewModel.recentSongs.collectAsState()
    val frequentArtists by viewModel.frequentArtists.collectAsState()
    val suggestedAlbums by viewModel.suggestedAlbums.collectAsState()
    val dailySong       by viewModel.dailySong.collectAsState()
    val allSongs         = viewModel.allSongs.collectAsState().value

    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text     = "vora",
                style    = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = (-1).sp
                ),
                color    = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
            )
        }

        dailySong?.let { song ->
            item {
                SectionTitle("Song of the day")
                DailySongCard(
                    song   = song,
                    onPlay = { playerController.play(song, allSongs) }
                )
            }
        }

        if (recentSongs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle("Recently played")
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recentSongs) { song ->
                        SongCard(
                            song    = song,
                            onClick = { playerController.play(song, recentSongs) }
                        )
                    }
                }
            }
        }

        if (frequentArtists.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle("Your artists")
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(frequentArtists) { artistName ->
                        ArtistChip(
                            name    = artistName,
                            onClick = { onOpenArtist(artistName) }
                        )
                    }
                }
            }
        }

        if (suggestedAlbums.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle("Suggested albums")
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(suggestedAlbums) { album ->
                        AlbumCard(
                            album   = album,
                            onClick = { onOpenAlbum(album.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(
    title    : String,
    onSeeAll : (() -> Unit)? = null
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(
                    text  = "See all",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DailySongCard(song: Song, onPlay: () -> Unit) {
    val bgColor = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onPlay() }
    ) {
        AsyncImage(
            model              = song.albumArtUri?.let {
                if (it.startsWith("/")) java.io.File(it) else it
            },
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f  to Color.Transparent,
                            0.45f to Color.Black.copy(alpha = 0.25f),
                            1.0f  to Color.Black.copy(alpha = 0.80f)
                        )
                    )
                )
        )

        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
            verticalAlignment     = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = "SONG OF THE DAY",
                    style    = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color    = Color.White.copy(alpha = 0.60f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text     = song.title,
                    style    = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color    = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text     = song.artist,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = Color.White.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            FilledIconButton(
                onClick  = onPlay,
                modifier = Modifier.size(44.dp),
                colors   = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.White,
                    contentColor   = Color.Black
                )
            ) {
                Icon(
                    imageVector        = Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    modifier           = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SongCard(song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model              = song.albumArtUri?.let {
                if (it.startsWith("/")) java.io.File(it) else it
            },
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text     = song.title,
            style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
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

@Composable
fun AlbumCard(album: Album, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model              = album.albumArtUri?.let {
                if (it.startsWith("/")) java.io.File(it) else it
            },
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text     = album.name,
            style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text     = album.artist,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ArtistChip(name: String, onClick: () -> Unit) {
    Row(
        modifier              = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text  = name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
