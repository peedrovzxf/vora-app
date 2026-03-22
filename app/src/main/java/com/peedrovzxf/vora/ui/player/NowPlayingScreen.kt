package com.peedrovzxf.vora.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.peedrovzxf.vora.player.PlayerController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    playerController: PlayerController,
    onDismiss: () -> Unit
) {
    val currentSong by playerController.currentSong.collectAsState()
    val isPlaying by playerController.isPlaying.collectAsState()
    val progress by playerController.progress.collectAsState()
    val duration by playerController.duration.collectAsState()
    val isShuffled by playerController.isShuffled.collectAsState()
    val repeatMode by playerController.repeatMode.collectAsState()

    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableStateOf(0f) }
    val displayProgress = if (isDragging) dragProgress else progress

    var showQueue by remember { mutableStateOf(false) }

    LaunchedEffect(progress) {
        if (isDragging && kotlin.math.abs(progress - dragProgress) < 0.02f) {
            isDragging = false
        }
    }

    val totalMinutes = (duration / 1000 / 60).toInt()
    val totalSeconds = (duration / 1000 % 60).toInt()

    if (showQueue) {
        ModalBottomSheet(
            onDismissRequest = { showQueue = false }
        ) {
            QueueScreen(
                playerController = playerController,
                onDismiss = { showQueue = false }
            )
        }
    }

    currentSong?.let { song ->
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = song.albumArtUri?.let {
                    if (it.startsWith("/")) java.io.File(it) else it
                },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(40.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Dismiss",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Now Playing",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    IconButton(onClick = { showQueue = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Queue",
                            tint = Color.White
                        )
                    }
                }

                AsyncImage(
                    model = song.albumArtUri?.let {
                        if (it.startsWith("/")) java.io.File(it) else it
                    },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Column {
                    Slider(
                        value = displayProgress,
                        onValueChange = {
                            isDragging = true
                            dragProgress = it
                        },
                        onValueChangeFinished = {
                            playerController.seekTo(dragProgress)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        thumb = {
                            SliderDefaults.Thumb(
                                interactionSource = remember { MutableInteractionSource() },
                                modifier = Modifier.size(20.dp),
                                colors = SliderDefaults.colors(thumbColor = Color.White)
                            )
                        },
                        track = { sliderState ->
                            SliderDefaults.Track(
                                sliderState = sliderState,
                                modifier = Modifier.height(4.dp),
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Color.White,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                )
                            )
                        }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val displayMs = (displayProgress * duration).toLong()
                        val displayMinutes = (displayMs / 1000 / 60).toInt()
                        val displaySeconds = (displayMs / 1000 % 60).toInt()
                        Text(
                            text = "%d:%02d".format(displayMinutes, displaySeconds),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "%d:%02d".format(totalMinutes, totalSeconds),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { playerController.toggleShuffle() }) {
                        Icon(
                            Icons.Filled.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffled) Color.White else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(onClick = { playerController.previous() }) {
                        Icon(
                            Icons.Filled.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    FilledIconButton(
                        onClick = { playerController.togglePlayPause() },
                        modifier = Modifier.size(64.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
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
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    IconButton(onClick = { playerController.toggleRepeat() }) {
                        Icon(
                            when (repeatMode) {
                                PlayerController.RepeatMode.ONE -> Icons.Filled.RepeatOne
                                else -> Icons.Filled.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = when (repeatMode) {
                                PlayerController.RepeatMode.NONE -> Color.White.copy(alpha = 0.4f)
                                else -> Color.White
                            },
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}