package com.peedrovzxf.vora

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.peedrovzxf.vora.data.local.MediaStoreSource
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.ui.theme.VoraTheme
import androidx.compose.foundation.clickable
import com.peedrovzxf.vora.player.PlayerController
import com.peedrovzxf.vora.ui.player.NowPlayingBar

class MainActivity : ComponentActivity() {

    private lateinit var playerController: PlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerController = PlayerController(this)
        enableEdgeToEdge()
        setContent {
            VoraTheme {
                Scaffold(
                    bottomBar = {
                        NowPlayingBar(playerController)
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        SongListScreen(playerController)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerController.release()
    }
}

@Composable
fun SongListScreen(playerController: PlayerController) {
    val context = LocalContext.current
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            songs = MediaStoreSource(context).getSongs()
        } else {
            launcher.launch(permission)
        }
    }

    if (hasPermission) {
        val songList = songs
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(songList) { song ->
                SongItem(song, playerController, songList)
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Se necesita permiso para acceder a la música")
        }
    }
}

@Composable
fun SongItem(song: Song, playerController: PlayerController, songs: List<Song>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { playerController.play(song, songs) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = song.title, style = MaterialTheme.typography.bodyLarge)
        Text(text = song.artist, style = MaterialTheme.typography.bodySmall)
    }
}