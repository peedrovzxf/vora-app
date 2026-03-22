package com.peedrovzxf.vora

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.peedrovzxf.vora.data.local.AppDatabase
import com.peedrovzxf.vora.data.local.MediaStoreSource
import com.peedrovzxf.vora.data.model.Album
import com.peedrovzxf.vora.data.model.Artist
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.data.repository.PlayHistoryRepository
import com.peedrovzxf.vora.data.repository.SongMetadataRepository
import com.peedrovzxf.vora.player.PlayerController
import com.peedrovzxf.vora.ui.navigation.NavGraph
import com.peedrovzxf.vora.ui.navigation.Screen
import com.peedrovzxf.vora.ui.player.NowPlayingScreen
import com.peedrovzxf.vora.ui.settings.SettingsViewModel
import com.peedrovzxf.vora.ui.theme.VoraTheme
import com.peedrovzxf.vora.ui.player.MiniPlayerBar

class MainActivity : ComponentActivity() {

    private lateinit var playerController: PlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val historyRepository = PlayHistoryRepository(
            AppDatabase.getInstance(this).playHistoryDao()
        )
        playerController = PlayerController(this, historyRepository)
        setContent {
            VoraApp(playerController)
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
    val settingsViewModel: SettingsViewModel = viewModel()
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val accentColor by settingsViewModel.accentColor.collectAsState()

    VoraTheme(darkTheme = isDarkMode, accentColor = accentColor) {
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
            return@VoraTheme
        }

        val currentSong by playerController.currentSong.collectAsState()
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        var isNowPlayingExpanded by remember { mutableStateOf(false) }

        val topLevelRoutes = listOf(
            Screen.Home.route,
            Screen.Search.route,
            Screen.Library.route,
            Screen.Settings.route
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    Column {
                        AnimatedVisibility(
                            visible = currentSong != null && !isNowPlayingExpanded,
                            enter = slideInVertically { it },
                            exit = slideOutVertically { it }
                        ) {
                            val showNavBar = currentRoute in topLevelRoutes
                            Box(
                                modifier = if (!showNavBar) Modifier.navigationBarsPadding() else Modifier
                            ) {
                                MiniPlayerBar(
                                    playerController = playerController,
                                    onTap = { isNowPlayingExpanded = true }
                                )
                            }
                        }
                        AnimatedVisibility(visible = currentRoute in topLevelRoutes) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Home.route,
                                    onClick = {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Home.route) { inclusive = true }
                                        }
                                    },
                                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                                    label = { Text("Home") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Search.route,
                                    onClick = {
                                        navController.navigate(Screen.Search.route) {
                                            popUpTo(Screen.Home.route)
                                        }
                                    },
                                    icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                                    label = { Text("Search") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Library.route,
                                    onClick = {
                                        navController.navigate(Screen.Library.route) {
                                            popUpTo(Screen.Home.route)
                                        }
                                    },
                                    icon = { Icon(Icons.Filled.LibraryMusic, contentDescription = "Library") },
                                    label = { Text("Library") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Settings.route,
                                    onClick = {
                                        navController.navigate(Screen.Settings.route) {
                                            popUpTo(Screen.Home.route)
                                        }
                                    },
                                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings") }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                android.util.Log.d("Vora", "innerPadding bottom: ${innerPadding.calculateBottomPadding()}")
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
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

            AnimatedVisibility(
                visible = isNowPlayingExpanded,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier.fillMaxSize()
            ) {
                NowPlayingScreen(
                    playerController = playerController,
                    onDismiss = { isNowPlayingExpanded = false }
                )
            }
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