package com.peedrovzxf.vora.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.player.PlayerController
import com.peedrovzxf.vora.ui.library.tabs.SongsTab

@Composable
fun LibraryScreen(
    songs: List<Song>,
    playerController: PlayerController
) {
    val tabs = listOf("Songs", "Albums", "Artists")
    var selectedTab by remember { mutableIntStateOf(0) }

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
            0 -> SongsTab(songs = songs, playerController = playerController)
            1 -> Text("Albums — coming soon")
            2 -> Text("Artists — coming soon")
        }
    }
}