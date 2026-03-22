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

sealed class Screen(val route: String) {
    object Library : Screen("library")
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
            LibraryScreen(
                songs = songs,
                albums = albums,
                artists = artists,
                playerController = playerController
            )
        }
    }
}