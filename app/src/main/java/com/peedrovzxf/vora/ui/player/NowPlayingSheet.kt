package com.peedrovzxf.vora.ui.player

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.peedrovzxf.vora.player.PlayerController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingSheet(
    playerController: PlayerController,
    sheetState: SheetState
) {
    val currentSong by playerController.currentSong.collectAsState()
    val isPlaying by playerController.isPlaying.collectAsState()
    val progress by playerController.progress.collectAsState()
    val duration by playerController.duration.collectAsState()
    val scope = rememberCoroutineScope()

    val isExpanded = sheetState.currentValue == SheetValue.Expanded

    val currentMs = (progress * duration).toLong()
    val currentMinutes = (currentMs / 1000 / 60).toInt()
    val currentSeconds = (currentMs / 1000 % 60).toInt()
    val totalMinutes = (duration / 1000 / 60).toInt()
    val totalSeconds = (duration / 1000 % 60).toInt()

    currentSong?.let { song ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            AnimatedVisibility(visible = !isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clickable {
                            scope.launch { sheetState.expand() }
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = song.albumArtUri?.let {
                            if (it.startsWith("/")) java.io.File(it) else it
                        },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
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
                    IconButton(onClick = { playerController.previous() }) {
                        Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous")
                    }
                    IconButton(onClick = { playerController.togglePlayPause() }) {
                        Icon(
                            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = { playerController.next() }) {
                        Icon(Icons.Filled.SkipNext, contentDescription = "Next")
                    }
                }
            }

            // Expanded content
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = {
                            scope.launch { sheetState.partialExpand() }
                        }) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Collapse")
                        }
                    }

                    AsyncImage(
                        model = song.albumArtUri?.let {
                            if (it.startsWith("/")) java.io.File(it) else it
                        },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Column {
                        Slider(
                            value = progress,
                            onValueChange = { playerController.seekTo(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "%d:%02d".format(currentMinutes, currentSeconds),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = "%d:%02d".format(totalMinutes, totalSeconds),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { playerController.previous() }) {
                            Icon(
                                Icons.Filled.SkipPrevious,
                                contentDescription = "Previous",
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        FilledIconButton(
                            onClick = { playerController.togglePlayPause() },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        IconButton(onClick = { playerController.next() }) {
                            Icon(
                                Icons.Filled.SkipNext,
                                contentDescription = "Next",
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}