package com.peedrovzxf.vora

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.peedrovzxf.vora.data.local.AppDatabase
import com.peedrovzxf.vora.data.local.MediaStoreSource
import com.peedrovzxf.vora.data.model.Album
import com.peedrovzxf.vora.data.model.Artist
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.data.repository.SongMetadataRepository
import com.peedrovzxf.vora.player.PlayerController
import com.peedrovzxf.vora.ui.navigation.NavGraph
import com.peedrovzxf.vora.ui.player.NowPlayingSheet
import com.peedrovzxf.vora.ui.theme.VoraTheme

class MainActivity : ComponentActivity() {

    private lateinit var playerController: PlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerController = PlayerController(this)
        enableEdgeToEdge()
        setContent {
            VoraTheme {
                VoraApp(playerController)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerController.release()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoraApp(playerController: PlayerController) {
    val context = LocalContext.current
    val navController = rememberNavController()
    var mergedSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var albums by remember { mutableStateOf<List<Album>>(emptyList()) }
    var artists by remember { mutableStateOf<List<Artist>>(emptyList()) }

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
    ) { granted -> hasPermission = granted }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            val source = MediaStoreSource(context)
            val rawSongs = source.getSongs()
            val metadataRepository = SongMetadataRepository(
                AppDatabase.getInstance(context).songMetadataDao()
            )
            metadataRepository.getAllSongsWithMetadata(rawSongs).collect { updated ->
                mergedSongs = updated
                albums = buildAlbums(updated)
                artists = buildArtists(updated)
            }
        } else {
            launcher.launch(permission)
        }
    }

    if (!hasPermission) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Se necesita permiso para acceder a la música")
        }
        return
    }

    val currentSong by playerController.currentSong.collectAsState()
    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val sheetPeekHeight = if (currentSong != null) 72.dp + navBarHeight else 0.dp

    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetPeekHeight = sheetPeekHeight,
        sheetDragHandle = null,
        sheetContent = {
            NowPlayingSheet(
                playerController = playerController,
                sheetState = sheetState.bottomSheetState
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            NavGraph(
                navController = navController,
                playerController = playerController,
                songs = mergedSongs,
                albums = albums,
                artists = artists
            )
        }
    }
}

fun buildAlbums(songs: List<Song>): List<Album> {
    return songs
        .groupBy { it.album }
        .map { (albumName, albumSongs) ->
            Album(
                id = albumSongs.first().id,
                name = albumName,
                artist = albumSongs.first().artist,
                albumArtUri = albumSongs.first().albumArtUri,
                songs = albumSongs
            )
        }
        .sortedBy { it.name }
}

fun buildArtists(songs: List<Song>): List<Artist> {
    val albums = buildAlbums(songs)
    return albums
        .groupBy { it.artist }
        .map { (artistName, artistAlbums) ->
            Artist(
                name = artistName,
                albums = artistAlbums,
                songs = artistAlbums.flatMap { it.songs }
            )
        }
        .sortedBy { it.name }
}