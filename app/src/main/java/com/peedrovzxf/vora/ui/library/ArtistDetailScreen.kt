package com.peedrovzxf.vora.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.peedrovzxf.vora.data.model.Artist
import com.peedrovzxf.vora.player.PlayerController

@Composable
fun ArtistDetailScreen(
    artist           : Artist,
    playerController : PlayerController,
    onOpenAlbum      : (albumId: Long) -> Unit,
    onBack           : () -> Unit
) {
    val bgColor = MaterialTheme.colorScheme.background

    LazyColumn(modifier = Modifier.fillMaxSize()) {

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                    bgColor
                                )
                            )
                        )
                )

                IconButton(
                    onClick  = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 8.dp, start = 4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint               = MaterialTheme.colorScheme.onBackground
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text(
                        text  = artist.name,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color    = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text  = buildString {
                            append("${artist.albums.size} ")
                            append(if (artist.albums.size == 1) "album" else "albums")
                            append("  ·  ")
                            append("${artist.songs.size} ")
                            append(if (artist.songs.size == 1) "song" else "songs")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick  = {
                        if (artist.songs.isNotEmpty())
                            playerController.play(artist.songs.first(), artist.songs)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape    = RoundedCornerShape(50)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play all", style = MaterialTheme.typography.labelLarge)
                }

                OutlinedButton(
                    onClick  = {
                        if (artist.songs.isNotEmpty()) {
                            val shuffled = artist.songs.shuffled()
                            playerController.play(shuffled.first(), shuffled)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape    = RoundedCornerShape(50)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Shuffle,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Shuffle", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (artist.albums.isNotEmpty()) {
            item {
                Text(
                    text     = "Albums",
                    style    = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(start = 20.dp, bottom = 10.dp)
                )
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(artist.albums) { album ->
                        Column(
                            modifier = Modifier
                                .width(130.dp)
                                .clickable { onOpenAlbum(album.id) }
                        ) {
                            AsyncImage(
                                model              = album.albumArtUri?.let {
                                    if (it.startsWith("/")) java.io.File(it) else it
                                },
                                contentDescription = album.name,
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier
                                    .size(130.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text     = album.name,
                                style    = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text     = "${album.songs.size} songs",
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        item {
            Text(
                text     = "Songs",
                style    = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(start = 20.dp, bottom = 4.dp)
            )
        }

        items(artist.songs) { song ->
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .clickable { playerController.play(song, artist.songs) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model              = song.albumArtUri?.let {
                        if (it.startsWith("/")) java.io.File(it) else it
                    },
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text     = song.title,
                        style    = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text     = song.album,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
