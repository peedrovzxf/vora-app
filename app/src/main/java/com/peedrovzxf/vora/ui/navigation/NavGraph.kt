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
import com.peedrovzxf.vora.ui.edit.EditSongScreen
import com.peedrovzxf.vora.ui.edit.EditSongViewModel
import com.peedrovzxf.vora.ui.library.AlbumDetailScreen
import com.peedrovzxf.vora.ui.library.ArtistDetailScreen
import com.peedrovzxf.vora.ui.playlist.PlaylistDetailScreen

sealed class Screen(val route: String) {
    object Library : Screen("library")
    object Playlists : Screen("playlists")
    object PlaylistDetail : Screen("playlist/{playlistId}/{playlistName}") {
        fun createRoute(playlistId: Long, playlistName: String) = "playlist/$playlistId/$playlistName"
    }
    object EditSong : Screen("edit/{songId}") {
        fun createRoute(songId: Long) = "edit/$songId"
    }
    object AlbumDetail : Screen("album/{albumId}") {
        fun createRoute(albumId: Long) = "album/$albumId"
    }

    object ArtistDetail : Screen("artist/{artistName}") {
        fun createRoute(artistName: String) = "artist/$artistName"
    }
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
                playlistViewModel = playlistViewModel,
                navController = navController
            )
        }

        composable(Screen.Playlists.route) {
            val playlistViewModel: PlaylistViewModel = viewModel()
            playlistViewModel.setSongs(songs)
            PlaylistScreen(
                viewModel = playlistViewModel,
                playerController = playerController,
                onOpenPlaylist = { playlistId, playlistName ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId, playlistName))
                }
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

        composable(Screen.AlbumDetail.route) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId")?.toLong() ?: return@composable
            val album = albums.find { it.id == albumId } ?: return@composable
            AlbumDetailScreen(
                album = album,
                playerController = playerController,
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
    }
}