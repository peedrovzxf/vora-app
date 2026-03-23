package com.peedrovzxf.vora.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.GraphicEq
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
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun QueueScreen(
    playerController: PlayerController,
    onDismiss: () -> Unit
) {
    val queue       by playerController.queue.collectAsState()
    val currentSong by playerController.currentSong.collectAsState()

    val lazyListState = rememberLazyListState()
    val reorderState  = rememberReorderableLazyListState(lazyListState) { from, to ->
        playerController.moveInQueue(from.index - 1, to.index - 1)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text  = "Queue",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text  = "${queue.size} ${if (queue.size == 1) "song" else "songs"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (queue.size > 1) {
                TextButton(
                    onClick = { playerController.clearQueue() },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.DeleteSweep,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text  = "Clear all",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            state           = lazyListState,
            modifier        = Modifier.fillMaxSize(),
            contentPadding  = PaddingValues(vertical = 8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(0.dp)) }

            itemsIndexed(queue, key = { _, song -> song.id }) { index, song ->
                val isCurrentSong = song.id == currentSong?.id

                ReorderableItem(reorderState, key = song.id) {
                    QueueItemRow(
                        title        = song.title,
                        artist       = song.artist,
                        artUri       = song.albumArtUri,
                        isCurrent    = isCurrentSong,
                        accentColor  = MaterialTheme.colorScheme.primary,
                        onRowClick   = { playerController.playFromQueue(index) },
                        onRemove     = if (!isCurrentSong) {
                            { playerController.removeFromQueue(index) }
                        } else null,
                        dragHandle   = {
                            Icon(
                                imageVector        = Icons.Filled.DragHandle,
                                contentDescription = "Drag to reorder",
                                modifier           = Modifier
                                    .draggableHandle()
                                    .size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueItemRow(
    title       : String,
    artist      : String,
    artUri      : String?,
    isCurrent   : Boolean,
    accentColor : Color,
    onRowClick  : () -> Unit,
    onRemove    : (() -> Unit)?,
    dragHandle  : @Composable () -> Unit
) {
    val bgColor = if (isCurrent) accentColor.copy(alpha = 0.08f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { onRowClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier        = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model              = artUri?.let {
                    if (it.startsWith("/")) java.io.File(it) else it
                },
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.50f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Filled.GraphicEq,
                        contentDescription = "Now playing",
                        tint               = accentColor,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = title,
                style    = MaterialTheme.typography.bodyLarge,
                color    = if (isCurrent) accentColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text     = artist,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (onRemove != null) {
            IconButton(
                onClick  = onRemove,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector        = Icons.Filled.Close,
                    contentDescription = "Remove from queue",
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(18.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(36.dp))
        }

        Spacer(modifier = Modifier.width(4.dp))

        dragHandle()
    }
}
