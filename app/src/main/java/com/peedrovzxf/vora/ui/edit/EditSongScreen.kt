package com.peedrovzxf.vora.ui.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.peedrovzxf.vora.data.local.ImageStorage
import com.peedrovzxf.vora.data.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSongScreen(
    song: Song,
    viewModel: EditSongViewModel,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(song.artist) }
    var album by remember { mutableStateOf(song.album) }
    var albumArtPath by remember { mutableStateOf(song.albumArtUri) }

    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = ImageStorage.saveAlbumArt(context, it, song.id)
            albumArtPath = savedPath
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit song") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.saveMetadata(
                            songId = song.id,
                            title = title,
                            artist = artist,
                            album = album,
                            albumArtPath = albumArtPath
                        )
                        onBack()
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = albumArtPath?.let {
                    if (it.startsWith("/")) java.io.File(it) else it
                },
                contentDescription = "Album art",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { imagePickerLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Change album art")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = artist,
                onValueChange = { artist = it },
                label = { Text("Artist") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = album,
                onValueChange = { album = it },
                label = { Text("Album") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    viewModel.clearMetadata(song.id)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset to original")
            }
        }
    }
}