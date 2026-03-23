package com.peedrovzxf.vora.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.peedrovzxf.vora.data.model.Album
import com.peedrovzxf.vora.player.PlayerController

@Composable
fun AlbumDetailScreen(
    album            : Album,
    playerController : PlayerController,
    onEditAlbum      : (albumId: Long) -> Unit,
    onBack           : () -> Unit
) {
    val viewModel: AlbumDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    viewModel.setAlbum(album)
    val currentAlbum by viewModel.album.collectAsState()
    val displayAlbum  = currentAlbum ?: album
    val bgColor       = MaterialTheme.colorScheme.background

    LazyColumn(modifier = Modifier.fillMaxSize()) {

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                AsyncImage(
                    model              = displayAlbum.albumArtUri?.let {
                        if (it.startsWith("/")) java.io.File(it) else it
                    },
                    contentDescription = displayAlbum.name,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f  to Color.Black.copy(alpha = 0.20f),
                                    0.50f to Color.Transparent,
                                    0.75f to bgColor.copy(alpha = 0.60f),
                                    1.0f  to bgColor
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
                        tint               = Color.White
                    )
                }

                IconButton(
                    onClick  = { onEditAlbum(displayAlbum.id) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Edit,
                        contentDescription = "Edit album",
                        tint               = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                ) {
                    Text(
                        text     = displayAlbum.name,
                        style    = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color    = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text     = displayAlbum.artist,
                        style    = MaterialTheme.typography.bodyLarge,
                        color    = Color.White.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = "${displayAlbum.songs.size} ${if (displayAlbum.songs.size == 1) "song" else "songs"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedIconButton(
                        onClick = {
                            if (displayAlbum.songs.isNotEmpty()) {
                                val shuffled = displayAlbum.songs.shuffled()
                                playerController.play(shuffled.first(), shuffled)
                            }
                        },
                        modifier = Modifier.size(44.dp),
                        border   = ButtonDefaults.outlinedButtonBorder(enabled = true)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Shuffle,
                            contentDescription = "Shuffle",
                            modifier           = Modifier.size(20.dp)
                        )
                    }

                    FilledIconButton(
                        onClick  = {
                            if (displayAlbum.songs.isNotEmpty())
                                playerController.play(displayAlbum.songs.first(), displayAlbum.songs)
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.PlayArrow,
                            contentDescription = "Play all",
                            modifier           = Modifier.size(22.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                color     = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
                modifier  = Modifier.padding(horizontal = 20.dp)
            )
        }

        items(displayAlbum.songs.withIndex().toList()) { (index, song) ->
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .clickable { playerController.play(song, displayAlbum.songs) }
                    .padding(horizontal = 20.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier         = Modifier.width(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = "${index + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text     = song.title,
                        style    = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (song.artist != displayAlbum.artist) {
                        Text(
                            text     = song.artist,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
