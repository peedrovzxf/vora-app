package com.peedrovzxf.vora.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.peedrovzxf.vora.data.model.Album
import com.peedrovzxf.vora.data.model.Artist
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.player.PlayerController

@Composable
fun SearchScreen(
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    playerController: PlayerController,
    onOpenAlbum: (Long) -> Unit,
    onOpenArtist: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filteredSongs = remember(query) {
        if (query.isBlank()) emptyList()
        else songs.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true)
        }.take(6)
    }

    val filteredAlbums = remember(query) {
        if (query.isBlank()) emptyList()
        else albums.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true)
        }.take(5)
    }

    val filteredArtists = remember(query) {
        if (query.isBlank()) emptyList()
        else artists.filter {
            it.name.contains(query, ignoreCase = true)
        }.take(4)
    }

    val hasResults = filteredSongs.isNotEmpty() || filteredAlbums.isNotEmpty() || filteredArtists.isNotEmpty()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text     = "Search",
            style    = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 16.dp)
        )

        TextField(
            value         = query,
            onValueChange = { query = it },
            placeholder   = {
                Text(
                    text  = "Songs, artists, albums…",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon   = {
                Icon(
                    imageVector        = Icons.Filled.Search,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(22.dp)
                )
            },
            trailingIcon  = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(
                            imageVector        = Icons.Filled.Close,
                            contentDescription = "Clear",
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine    = true,
            shape         = RoundedCornerShape(50),
            colors        = TextFieldDefaults.colors(
                focusedContainerColor          = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor        = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor         = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor          = Color.Transparent,
                unfocusedIndicatorColor        = Color.Transparent,
                disabledIndicatorColor         = Color.Transparent,
            ),
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        when {
            query.isBlank() -> EmptySearchState()
            !hasResults     -> NoResultsState(query = query)
            else -> {
                LazyColumn(
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    if (filteredSongs.isNotEmpty()) {
                        item {
                            SearchSectionHeader(title = "Songs")
                        }
                        items(filteredSongs) { song ->
                            SearchSongRow(
                                title    = song.title,
                                subtitle = song.artist,
                                artUri   = song.albumArtUri,
                                onClick  = { playerController.play(song, songs) }
                            )
                        }
                    }

                    if (filteredAlbums.isNotEmpty()) {
                        item {
                            SearchSectionHeader(title = "Albums")
                        }
                        items(filteredAlbums) { album ->
                            SearchSongRow(
                                title    = album.name,
                                subtitle = album.artist,
                                artUri   = album.albumArtUri,
                                onClick  = { onOpenAlbum(album.id) }
                            )
                        }
                    }

                    if (filteredArtists.isNotEmpty()) {
                        item {
                            SearchSectionHeader(title = "Artists")
                        }
                        items(filteredArtists) { artist ->
                            SearchArtistRow(
                                name    = artist.name,
                                stats   = "${artist.albums.size} albums · ${artist.songs.size} songs",
                                onClick = { onOpenArtist(artist.name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySearchState() {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Filled.MusicNote,
                contentDescription = null,
                modifier           = Modifier.size(52.dp),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Text(
                text  = "What do you want to hear?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun NoResultsState(query: String) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text  = "No results for",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = "\"$query\"",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SearchSectionHeader(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 4.dp)
    )
}

@Composable
private fun SearchSongRow(
    title    : String,
    subtitle : String,
    artUri   : String?,
    onClick  : () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model              = artUri?.let {
                if (it.startsWith("/")) java.io.File(it) else it
            },
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = title,
                style    = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text     = subtitle,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SearchArtistRow(
    name    : String,
    stats   : String,
    onClick : () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape    = CircleShape,
                color    = MaterialTheme.colorScheme.surfaceVariant
            ) {}
            Text(
                text  = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = name,
                style    = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text     = stats,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
