package com.peedrovzxf.vora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.peedrovzxf.vora.data.model.Album
import com.peedrovzxf.vora.data.model.Artist
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.player.PlayerController
import com.peedrovzxf.vora.ui.library.LibraryScreen
import com.peedrovzxf.vora.ui.playlist.PlaylistScreen
import com.peedrovzxf.vora.ui.playlist.PlaylistViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

sealed class Screen(val route: String) {
    object Library : Screen("library")
    object Playlists : Screen("playlists")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    playerController: PlayerController,
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>
) {
    NavHost(navController = navController, startDestination = Screen.Library.route) {
        composable(Screen.Library.route) {
            val playlistViewModel: PlaylistViewModel = viewModel()
            playlistViewModel.setSongs(songs)
            LibraryScreen(
                songs = songs,
                albums = albums,
                artists = artists,
                playerController = playerController,
                playlistViewModel = playlistViewModel
            )
        }

        composable(Screen.Playlists.route) {
            val playlistViewModel: PlaylistViewModel = viewModel()
            playlistViewModel.setSongs(songs)
            PlaylistScreen(
                viewModel = playlistViewModel,
                playerController = playerController
            )
        }
    }
}