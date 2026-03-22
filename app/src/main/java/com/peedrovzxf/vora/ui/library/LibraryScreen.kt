package com.peedrovzxf.vora.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.peedrovzxf.vora.data.model.Album
import com.peedrovzxf.vora.data.model.Artist
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.player.PlayerController
import com.peedrovzxf.vora.ui.library.tabs.AlbumsTab
import com.peedrovzxf.vora.ui.library.tabs.ArtistsTab
import com.peedrovzxf.vora.ui.library.tabs.SongsTab
import com.peedrovzxf.vora.ui.playlist.PlaylistScreen
import com.peedrovzxf.vora.ui.playlist.PlaylistViewModel

@Composable
fun LibraryScreen(
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    playerController: PlayerController,
    playlistViewModel: PlaylistViewModel
) {
    val tabs = listOf("Songs", "Albums", "Artists", "Playlists")
    var selectedTab by remember { mutableIntStateOf(0) }
    val playlists by playlistViewModel.playlists.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> SongsTab(
                songs = songs,
                playerController = playerController,
                playlists = playlists,
                onAddToPlaylist = { playlistId, songId ->
                    playlistViewModel.addSongToPlaylist(playlistId, songId)
                }
            )
            1 -> AlbumsTab(albums = albums, playerController = playerController)
            2 -> ArtistsTab(artists = artists, playerController = playerController)
            3 -> PlaylistScreen(viewModel = playlistViewModel, playerController = playerController)
        }
    }
}