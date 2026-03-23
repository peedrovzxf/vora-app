package com.peedrovzxf.vora.ui.liked

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.player.PlayerController

@Composable
fun LikedSongsScreen(
    songs            : List<Song>,
    playerController : PlayerController,
    onBack           : () -> Unit,
    likedViewModel   : LikedSongsViewModel = viewModel()
) {
    val likedIds   by likedViewModel.likedIds.collectAsState()
    val likedSongs  = remember(likedIds, songs) {
        songs.filter { it.id in likedIds }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.40f),
                                    MaterialTheme.colorScheme.background
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
                    modifier            = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text  = "Liked Songs",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text  = "${likedSongs.size} ${if (likedSongs.size == 1) "song" else "songs"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (likedSongs.isNotEmpty()) {
            item {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick  = { playerController.play(likedSongs.first(), likedSongs) },
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
                            val shuffled = likedSongs.shuffled()
                            playerController.play(shuffled.first(), shuffled)
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

                HorizontalDivider(
                    color     = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp,
                    modifier  = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
        }

        if (likedSongs.isEmpty()) {
            item {
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Favorite,
                            contentDescription = null,
                            modifier           = Modifier.size(52.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                        )
                        Text(
                            text  = "No liked songs yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text  = "Tap the heart in Now Playing to save songs here",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        items(likedSongs, key = { it.id }) { song ->
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .clickable { playerController.play(song, likedSongs) }
                    .padding(start = 20.dp, end = 8.dp, top = 9.dp, bottom = 9.dp),
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
                        style    = MaterialTheme.typography.bodyLarge,
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

                IconButton(
                    onClick  = { likedViewModel.toggle(song.id) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Favorite,
                        contentDescription = "Unlike",
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
