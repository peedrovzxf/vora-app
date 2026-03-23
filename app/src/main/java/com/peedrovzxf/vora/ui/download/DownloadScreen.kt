package com.peedrovzxf.vora.ui.download

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.peedrovzxf.vora.data.youtube.YoutubeSearchResult

@Composable
fun DownloadScreen(
    downloadViewModel: DownloadViewModel
) {
    val searchState    by downloadViewModel.searchState.collectAsState()
    val itemStates     by downloadViewModel.itemStates.collectAsState()
    val playlistStates by downloadViewModel.playlistStates.collectAsState()
    val apiKeyState    by downloadViewModel.apiKeyState.collectAsState()
    var query          by remember { mutableStateOf("") }
    val focusManager   = LocalFocusManager.current

    fun triggerSearch() {
        focusManager.clearFocus()
        downloadViewModel.search(query)
    }

    if (apiKeyState is ApiKeyState.Missing) {
        ApiKeyDialog(
            onConfirm = { key -> downloadViewModel.saveApiKey(key) },
            onDismiss = {}
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 20.dp, bottom = 16.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text     = "Downloads",
                style    = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            if (apiKeyState is ApiKeyState.Present) {
                IconButton(onClick = { downloadViewModel.clearApiKey() }) {
                    Icon(
                        imageVector        = Icons.Filled.Key,
                        contentDescription = "Cambiar API Key",
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        TextField(
            value         = query,
            onValueChange = { query = it },
            placeholder   = { Text("Buscar en YouTube…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon   = {
                Icon(Icons.Filled.Search, null,
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp))
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = ""; downloadViewModel.search("") }) {
                        Icon(Icons.Filled.Close, "Clear",
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp))
                    }
                }
            },
            enabled         = apiKeyState is ApiKeyState.Present,
            singleLine      = true,
            shape           = RoundedCornerShape(50),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { triggerSearch() }),
            colors          = TextFieldDefaults.colors(
                focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor  = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor  = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedContent(
            targetState    = searchState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label          = "searchContent"
        ) { state ->
            when (state) {
                is SearchUiState.Idle    -> IdleHint()

                is SearchUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color    = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                is SearchUiState.Error -> {
                    Box(
                        modifier         = Modifier.fillMaxSize().padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.ErrorOutline, null,
                                modifier = Modifier.size(48.dp),
                                tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            Text("No se pudo conectar",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                            Text(state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            OutlinedButton(onClick = { triggerSearch() }, shape = RoundedCornerShape(50)) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                is SearchUiState.Results -> {
                    if (state.items.isEmpty()) {
                        Box(
                            modifier         = Modifier.fillMaxSize().padding(bottom = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sin resultados para \"$query\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier       = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(state.items, key = { result ->
                                when (result) {
                                    is YoutubeSearchResult.Video    -> "v_${result.videoId}"
                                    is YoutubeSearchResult.Playlist -> "p_${result.playlistId}"
                                }
                            }) { result ->
                                when (result) {
                                    is YoutubeSearchResult.Video -> SearchVideoItem(
                                        item          = result,
                                        downloadState = itemStates[result.videoId] ?: ItemDownloadState.NONE,
                                        onDownload    = { downloadViewModel.downloadVideo(result) }
                                    )
                                    is YoutubeSearchResult.Playlist -> SearchPlaylistItem(
                                        item          = result,
                                        downloadState = playlistStates[result.playlistId] ?: ItemDownloadState.NONE,
                                        onDownload    = { downloadViewModel.downloadPlaylist(result) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApiKeyDialog(
    onConfirm : (String) -> Unit,
    onDismiss : () -> Unit
) {
    var input    by remember { mutableStateOf("") }
    var showKey  by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon  = { Icon(Icons.Filled.Key, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("YouTube API Key") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text  = "Para buscar y descargar canciones necesitas una API Key de YouTube Data v3. Puedes obtenerla gratis en Google Cloud Console.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value         = input,
                    onValueChange = { input = it; hasError = false },
                    label         = { Text("API Key") },
                    singleLine    = true,
                    isError       = hasError,
                    supportingText = if (hasError) {{ Text("La key no puede estar vacía") }} else null,
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                if (showKey) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (input.isBlank()) hasError = true else onConfirm(input)
            }) { Text("Guardar") }
        },
        dismissButton = null
    )
}

@Composable
private fun SearchVideoItem(
    item          : YoutubeSearchResult.Video,
    downloadState : ItemDownloadState,
    onDownload    : () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model              = item.thumbnailUrl,
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier
                .size(width = 100.dp, height = 64.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title,
                style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(item.channelName,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            if (item.durationSeconds > 0L) {
                Text(item.formattedDuration,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        }
        Spacer(Modifier.width(8.dp))
        DownloadButton(state = downloadState, onDownload = onDownload)
    }
}

@Composable
private fun SearchPlaylistItem(
    item          : YoutubeSearchResult.Playlist,
    downloadState : ItemDownloadState,
    onDownload    : () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier
            .size(width = 100.dp, height = 64.dp)
            .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model              = item.thumbnailUrl,
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            ) {
                Icon(
                    imageVector        = Icons.Filled.LibraryMusic,
                    contentDescription = "Playlist",
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier
                        .padding(2.dp)
                        .size(14.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.title,
                style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(item.channelName,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Text("Playlist",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
        }

        Spacer(Modifier.width(8.dp))
        DownloadButton(state = downloadState, onDownload = onDownload)
    }
}

@Composable
private fun DownloadButton(
    state      : ItemDownloadState,
    onDownload : () -> Unit
) {
    Box(Modifier.size(40.dp), Alignment.Center) {
        when (state) {
            ItemDownloadState.NONE -> {
                IconButton(onClick = onDownload, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Filled.CloudDownload, "Descargar",
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp))
                }
            }
            ItemDownloadState.FETCHING,
            ItemDownloadState.DOWNLOADING -> {
                CircularProgressIndicator(
                    modifier    = Modifier.size(24.dp),
                    color       = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.5.dp)
            }
            ItemDownloadState.DONE -> {
                Icon(Icons.Filled.Check, "Descargado",
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp))
            }
            ItemDownloadState.ERROR -> {
                IconButton(onClick = onDownload, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Filled.ErrorOutline, "Error — reintentar",
                        tint     = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun IdleHint() {
    Box(
        modifier         = Modifier.fillMaxSize().padding(bottom = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.CloudDownload, null,
                modifier = Modifier.size(52.dp),
                tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
            Text("Busca una canción o álbum",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Text("Los archivos se guardan en Música/Vora",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }
    }
}