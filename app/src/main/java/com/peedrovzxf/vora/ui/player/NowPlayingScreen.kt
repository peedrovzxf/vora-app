package com.peedrovzxf.vora.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peedrovzxf.vora.ui.liked.LikedSongsViewModel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import coil.compose.AsyncImage
import com.peedrovzxf.vora.player.PlayerController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    playerController : PlayerController,
    onDismiss        : () -> Unit,
    likedViewModel   : LikedSongsViewModel = viewModel()
) {
    val currentSong  by playerController.currentSong.collectAsState()
    val isPlaying    by playerController.isPlaying.collectAsState()
    val progress     by playerController.progress.collectAsState()
    val duration     by playerController.duration.collectAsState()
    val isShuffled   by playerController.isShuffled.collectAsState()
    val repeatMode   by playerController.repeatMode.collectAsState()

    val likedIds     by likedViewModel.likedIds.collectAsState()
    val liked         = currentSong?.id?.let { it in likedIds } ?: false

    var isDragging   by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableStateOf(0f) }
    var showQueue    by remember { mutableStateOf(false) }

    val displayProgress = if (isDragging) dragProgress else progress

    LaunchedEffect(progress) {
        if (isDragging && kotlin.math.abs(progress - dragProgress) < 0.02f) {
            isDragging = false
        }
    }

    val configuration  = LocalConfiguration.current
    val screenWidthDp  = configuration.screenWidthDp.dp
    val artworkSize    = min(screenWidthDp * 0.82f, 360.dp)

    val totalMs      = duration
    val totalMin     = (totalMs / 1000 / 60).toInt()
    val totalSec     = (totalMs / 1000 % 60).toInt()
    val displayMs    = (displayProgress * duration).toLong()
    val displayMin   = (displayMs / 1000 / 60).toInt()
    val displaySec   = (displayMs / 1000 % 60).toInt()

    if (showQueue) {
        ModalBottomSheet(
            onDismissRequest = { showQueue = false },
            containerColor   = MaterialTheme.colorScheme.surface,
        ) {
            QueueScreen(
                playerController = playerController,
                onDismiss        = { showQueue = false }
            )
        }
    }

    currentSong?.let { song ->
        Box(modifier = Modifier.fillMaxSize()) {

            AsyncImage(
                model              = song.albumArtUri?.let {
                    if (it.startsWith("/")) java.io.File(it) else it
                },
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxSize()
                    .blur(50.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.62f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment   = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector        = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Dismiss",
                            tint               = Color.White,
                            modifier           = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text  = "Now Playing",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.55f),
                    )
                    IconButton(onClick = { showQueue = true }) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Queue",
                            tint               = Color.White,
                            modifier           = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                AsyncImage(
                    model              = song.albumArtUri?.let {
                        if (it.startsWith("/")) java.io.File(it) else it
                    },
                    contentDescription = song.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .size(artworkSize)
                        .shadow(
                            elevation    = 40.dp,
                            shape        = RoundedCornerShape(20.dp),
                            spotColor    = Color.Black,
                            ambientColor = Color.Black
                        )
                        .clip(RoundedCornerShape(20.dp))
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text     = song.title,
                            style    = MaterialTheme.typography.headlineSmall,
                            color    = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text     = song.artist,
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = Color.White.copy(alpha = 0.60f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = { currentSong?.let { likedViewModel.toggle(it.id) } }) {
                        Icon(
                            imageVector        = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (liked) "Unlike" else "Like",
                            tint               = if (liked) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.60f),
                            modifier           = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value                = displayProgress,
                        onValueChange        = {
                            isDragging   = true
                            dragProgress = it
                        },
                        onValueChangeFinished = {
                            playerController.seekTo(dragProgress)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        thumb = {
                            SliderDefaults.Thumb(
                                interactionSource = remember { MutableInteractionSource() },
                                modifier          = Modifier.size(14.dp),
                                colors            = SliderDefaults.colors(thumbColor = Color.White)
                            )
                        },
                        track = { sliderState ->
                            SliderDefaults.Track(
                                sliderState = sliderState,
                                modifier    = Modifier.height(3.dp),
                                colors      = SliderDefaults.colors(
                                    activeTrackColor   = Color.White,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.25f),
                                    thumbColor         = Color.White
                                )
                            )
                        }
                    )
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .offset(y = (-6).dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text  = "%d:%02d".format(displayMin, displaySec),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                        Text(
                            text  = "%d:%02d".format(totalMin, totalSec),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { playerController.toggleShuffle() }) {
                        Icon(
                            imageVector        = Icons.Filled.Shuffle,
                            contentDescription = "Shuffle",
                            tint               = if (isShuffled) Color.White else Color.White.copy(alpha = 0.35f),
                            modifier           = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick  = { playerController.previous() },
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.SkipPrevious,
                            contentDescription = "Previous",
                            tint               = Color.White,
                            modifier           = Modifier.size(34.dp)
                        )
                    }

                    FilledIconButton(
                        onClick  = { playerController.togglePlayPause() },
                        modifier = Modifier.size(68.dp),
                        colors   = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.White,
                            contentColor   = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector        = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier           = Modifier.size(34.dp)
                        )
                    }

                    IconButton(
                        onClick  = { playerController.next() },
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.SkipNext,
                            contentDescription = "Next",
                            tint               = Color.White,
                            modifier           = Modifier.size(34.dp)
                        )
                    }

                    IconButton(onClick = { playerController.toggleRepeat() }) {
                        Icon(
                            imageVector = when (repeatMode) {
                                PlayerController.RepeatMode.ONE -> Icons.Filled.RepeatOne
                                else                            -> Icons.Filled.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = when (repeatMode) {
                                PlayerController.RepeatMode.NONE -> Color.White.copy(alpha = 0.35f)
                                else                             -> Color.White
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.5f))
            }
        }
    }
}
