package com.peedrovzxf.vora.ui.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.peedrovzxf.vora.data.local.ImageStorage
import com.peedrovzxf.vora.data.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSongScreen(
    song      : Song,
    viewModel : EditSongViewModel,
    onBack    : () -> Unit
) {
    var title       by remember { mutableStateOf(song.title) }
    var artist      by remember { mutableStateOf(song.artist) }
    var album       by remember { mutableStateOf(song.album) }
    var albumArtPath by remember { mutableStateOf(song.albumArtUri) }

    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = ImageStorage.saveAlbumArt(context, it, song.id)
            albumArtPath  = savedPath
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Edit song",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveMetadata(
                                songId       = song.id,
                                title        = title.trim(),
                                artist       = artist.trim(),
                                album        = album.trim(),
                                albumArtPath = albumArtPath
                            )
                            onBack()
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text(
                            text  = "Save",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = if (title.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier         = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (albumArtPath != null) {
                    AsyncImage(
                        model              = albumArtPath?.let {
                            if (it.startsWith("/")) java.io.File(it) else it
                        },
                        contentDescription = "Album art",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier         = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.AddPhotoAlternate,
                            contentDescription = "Change artwork",
                            tint               = Color.White,
                            modifier           = Modifier.size(32.dp)
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape    = RoundedCornerShape(16.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector        = Icons.Filled.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier           = Modifier.size(36.dp),
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    text  = "Add artwork",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            EditField(
                value         = title,
                onValueChange = { title = it },
                label         = "Title",
                placeholder   = "Song title"
            )

            Spacer(modifier = Modifier.height(12.dp))

            EditField(
                value         = artist,
                onValueChange = { artist = it },
                label         = "Artist",
                placeholder   = "Artist name"
            )

            Spacer(modifier = Modifier.height(12.dp))

            EditField(
                value         = album,
                onValueChange = { album = it },
                label         = "Album",
                placeholder   = "Album name"
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedButton(
                onClick  = {
                    viewModel.clearMetadata(song.id)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border   = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                )
            ) {
                Text(
                    text  = "Reset to original",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EditField(
    value         : String,
    onValueChange : (String) -> Unit,
    label         : String,
    placeholder   : String
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        placeholder   = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
        singleLine    = true,
        shape         = RoundedCornerShape(12.dp),
        modifier      = Modifier.fillMaxWidth()
    )
}
