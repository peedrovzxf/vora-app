package com.peedrovzxf.vora.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.peedrovzxf.vora.player.PlayerController

@Composable
fun NowPlayingBar(
    playerController: PlayerController,
    onTap: () -> Unit
) {
    val currentSong by playerController.currentSong.collectAsState()
    val isPlaying   by playerController.isPlaying.collectAsState()
    val progress    by playerController.progress.collectAsState()

    currentSong?.let { song ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Surface(
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(18.dp),
                color           = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation  = 6.dp,
                shadowElevation = 20.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTap() }
                            .padding(start = 10.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = song.albumArtUri?.let {
                                if (it.startsWith("/")) java.io.File(it) else it
                            },
                            contentDescription = null,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text     = song.title,
                                style    = MaterialTheme.typography.titleSmall,
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
                        IconButton(onClick = { playerController.togglePlayPause() }) {
                            Icon(
                                imageVector        = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier           = Modifier.size(26.dp)
                            )
                        }
                        IconButton(onClick = { playerController.next() }) {
                            Icon(
                                imageVector        = Icons.Filled.SkipNext,
                                contentDescription = "Next",
                                modifier           = Modifier.size(26.dp)
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress       = { progress },
                        modifier       = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                        color          = MaterialTheme.colorScheme.primary,
                        trackColor     = Color.Transparent
                    )
                }
            }
        }
    }
}
