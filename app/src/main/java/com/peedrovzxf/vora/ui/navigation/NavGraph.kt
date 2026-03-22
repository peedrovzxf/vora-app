package com.peedrovzxf.vora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.peedrovzxf.vora.data.model.Album
import com.peedrovzxf.vora.data.model.Artist
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.player.PlayerController
import com.peedrovzxf.vora.ui.edit.EditAlbumScreen
import com.peedrovzxf.vora.ui.edit.EditAlbumViewModel
import com.peedrovzxf.vora.ui.edit.EditSongScreen
import com.peedrovzxf.vora.ui.edit.EditSongViewModel
import com.peedrovzxf.vora.ui.home.HomeScreen
import com.peedrovzxf.vora.ui.home.HomeViewModel
import com.peedrovzxf.vora.ui.library.AlbumDetailScreen
import com.peedrovzxf.vora.ui.library.AlbumDetailViewModel
import com.peedrovzxf.vora.ui.library.ArtistDetailScreen
import com.peedrovzxf.vora.ui.library.LibraryScreen
import com.peedrovzxf.vora.ui.playlist.PlaylistDetailScreen
import com.peedrovzxf.vora.ui.playlist.PlaylistViewModel
import com.peedrovzxf.vora.ui.search.SearchScreen
import com.peedrovzxf.vora.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Library : Screen("library")
    object Settings : Screen("settings")
    object AlbumDetail : Screen("album/{albumId}") {
        fun createRoute(albumId: Long) = "album/$albumId"
    }
    object ArtistDetail : Screen("artist/{artistName}") {
        fun createRoute(artistName: String) = "artist/$artistName"
    }
    object PlaylistDetail : Screen("playlist/{playlistId}/{playlistName}") {
        fun createRoute(playlistId: Long, playlistName: String) = "playlist/$playlistId/$playlistName"
    }
    object EditSong : Screen("edit/{songId}") {
        fun createRoute(songId: Long) = "edit/$songId"
    }
    object EditAlbum : Screen("editalbum/{albumId}") {
        fun createRoute(albumId: Long) = "editalbum/$albumId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    playerController: PlayerController,
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel()
            homeViewModel.setData(songs, albums, artists)
            HomeScreen(
                viewModel = homeViewModel,
                playerController = playerController,
                onOpenAlbum = { albumId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                },
                onOpenArtist = { artistName ->
                    navController.navigate(Screen.ArtistDetail.createRoute(artistName))
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                songs = songs,
                albums = albums,
                artists = artists,
                playerController = playerController,
                onOpenAlbum = { albumId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                },
                onOpenArtist = { artistName ->
                    navController.navigate(Screen.ArtistDetail.createRoute(artistName))
                }
            )
        }

        composable(Screen.Library.route) {
            val playlistViewModel: PlaylistViewModel = viewModel()
            playlistViewModel.setSongs(songs)
            LibraryScreen(
                songs = songs,
                albums = albums,
                artists = artists,
                playerController = playerController,
                playlistViewModel = playlistViewModel,
                navController = navController
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        composable(Screen.AlbumDetail.route) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId")?.toLong() ?: return@composable
            val album = albums.find { it.id == albumId } ?: return@composable
            val viewModel: AlbumDetailViewModel = viewModel()
            viewModel.setAlbum(album)
            val albumDetailViewModel: AlbumDetailViewModel = viewModel()
            albumDetailViewModel.setAlbum(album)
            AlbumDetailScreen(
                album = album,
                playerController = playerController,
                onEditAlbum = { navController.navigate(Screen.EditAlbum.createRoute(it)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ArtistDetail.route) { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName") ?: return@composable
            val artist = artists.find { it.name == artistName } ?: return@composable
            ArtistDetailScreen(
                artist = artist,
                playerController = playerController,
                onOpenAlbum = { albumId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PlaylistDetail.route) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLong() ?: return@composable
            val playlistName = backStackEntry.arguments?.getString("playlistName") ?: return@composable
            val playlistViewModel: PlaylistViewModel = viewModel()
            playlistViewModel.setSongs(songs)
            PlaylistDetailScreen(
                playlistId = playlistId,
                playlistName = playlistName,
                viewModel = playlistViewModel,
                playerController = playerController,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EditSong.route) { backStackEntry ->
            val songId = backStackEntry.arguments?.getString("songId")?.toLong() ?: return@composable
            val song = songs.find { it.id == songId } ?: return@composable
            val editViewModel: EditSongViewModel = viewModel()
            editViewModel.setSong(song)
            EditSongScreen(
                song = song,
                viewModel = editViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EditAlbum.route) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId")?.toLong() ?: return@composable
            val album = albums.find { it.id == albumId } ?: return@composable
            val editAlbumViewModel: EditAlbumViewModel = viewModel()
            val albumDetailViewModel: AlbumDetailViewModel = viewModel()
            albumDetailViewModel.setAlbum(album)
            val currentAlbum by albumDetailViewModel.album.collectAsState()
            val displayAlbum = currentAlbum ?: album
            EditAlbumScreen(
                album = displayAlbum,
                viewModel = editAlbumViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}