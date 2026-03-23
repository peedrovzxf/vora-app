package com.peedrovzxf.vora.ui.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.peedrovzxf.vora.data.model.Album
import com.peedrovzxf.vora.data.model.Artist
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.player.PlayerController
import com.peedrovzxf.vora.ui.library.tabs.AlbumsTab
import com.peedrovzxf.vora.ui.library.tabs.ArtistsTab
import com.peedrovzxf.vora.ui.library.tabs.SongsTab
import com.peedrovzxf.vora.ui.navigation.Screen
import com.peedrovzxf.vora.ui.playlist.PlaylistScreen
import com.peedrovzxf.vora.ui.playlist.PlaylistViewModel

@Composable
fun LibraryScreen(
    songs            : List<Song>,
    albums           : List<Album>,
    artists          : List<Artist>,
    playerController : PlayerController,
    playlistViewModel: PlaylistViewModel,
    navController    : NavHostController
) {
    val tabs        = listOf("Songs", "Albums", "Artists", "Playlists")
    var selectedTab by remember { mutableIntStateOf(0) }
    val playlists   by playlistViewModel.playlists.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text     = "Library",
            style    = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 4.dp)
        )

        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, title ->
                PillTab(
                    label      = title,
                    isSelected = selectedTab == index,
                    onClick    = { selectedTab = index }
                )
            }
        }

        HorizontalDivider(
            color     = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp
        )

        when (selectedTab) {
            0 -> SongsTab(
                songs            = songs,
                playerController = playerController,
                playlists        = playlists,
                onAddToPlaylist  = { playlistId, songId ->
                    playlistViewModel.addSongToPlaylist(playlistId, songId)
                },
                onEditSong       = { songId ->
                    navController.navigate(Screen.EditSong.createRoute(songId))
                },
                onAddToQueue     = { song ->
                    playerController.addToQueue(song)
                }
            )
            1 -> AlbumsTab(
                albums           = albums,
                playerController = playerController,
                onOpenAlbum      = { albumId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                },
                onEditAlbum      = { albumId ->
                    navController.navigate(Screen.EditAlbum.createRoute(albumId))
                }
            )
            2 -> ArtistsTab(
                artists          = artists,
                playerController = playerController,
                onOpenArtist     = { artistName ->
                    navController.navigate(Screen.ArtistDetail.createRoute(artistName))
                }
            )
            3 -> PlaylistScreen(
                viewModel        = playlistViewModel,
                playerController = playerController,
                onOpenPlaylist   = { playlistId, playlistName ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId, playlistName))
                },
                onOpenLikedSongs = {
                    navController.navigate(Screen.LikedSongs.route)
                }
            )
        }
    }
}

@Composable
private fun PillTab(
    label      : String,
    isSelected : Boolean,
    onClick    : () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.0f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "tabBg"
    )

    val textColor by animateColorAsState(
        targetValue   = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "tabText"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = textColor
        )
    }
}
