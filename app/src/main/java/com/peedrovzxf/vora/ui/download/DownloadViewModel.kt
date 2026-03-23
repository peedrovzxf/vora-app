package com.peedrovzxf.vora.ui.download

import android.app.Application
import android.media.MediaScannerConnection
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peedrovzxf.vora.data.local.AppDatabase
import com.peedrovzxf.vora.data.youtube.YoutubeRepository
import com.peedrovzxf.vora.data.youtube.YoutubeSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import org.jaudiotagger.tag.mp4.Mp4Tag
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

sealed class SearchUiState {
    object Idle    : SearchUiState()
    object Loading : SearchUiState()
    data class Results(val items: List<YoutubeSearchResult>) : SearchUiState()
    data class Error(val message: String)                    : SearchUiState()
}

sealed class ApiKeyState {
    object Loading                      : ApiKeyState()
    object Missing                      : ApiKeyState()
    data class Present(val key: String) : ApiKeyState()
}

enum class ItemDownloadState { NONE, FETCHING, DOWNLOADING, DONE, ERROR }

class DownloadViewModel(application: Application) : AndroidViewModel(application) {

    private val db         = AppDatabase.getInstance(application)
    private val repository = YoutubeRepository(db)

    private val _refreshLibrary = MutableStateFlow(0)
    val refreshLibrary: StateFlow<Int> = _refreshLibrary

    private val _searchState    = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchState: StateFlow<SearchUiState> = _searchState

    private val _itemStates     = MutableStateFlow<Map<String, ItemDownloadState>>(emptyMap())
    val itemStates: StateFlow<Map<String, ItemDownloadState>> = _itemStates

    private val _playlistStates = MutableStateFlow<Map<String, ItemDownloadState>>(emptyMap())
    val playlistStates: StateFlow<Map<String, ItemDownloadState>> = _playlistStates

    private val _apiKeyState    = MutableStateFlow<ApiKeyState>(ApiKeyState.Loading)
    val apiKeyState: StateFlow<ApiKeyState> = _apiKeyState

    private var searchJob: Job? = null

    private val CHUNK_COUNT = 8

    init { loadApiKey() }

    private fun loadApiKey() {
        viewModelScope.launch {
            val key = repository.getApiKey()
            _apiKeyState.value = if (key.isNullOrBlank()) ApiKeyState.Missing
            else ApiKeyState.Present(key)
        }
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            repository.saveApiKey(key)
            _apiKeyState.value = ApiKeyState.Present(key.trim())
        }
    }

    fun clearApiKey() {
        viewModelScope.launch {
            repository.deleteApiKey()
            _apiKeyState.value = ApiKeyState.Missing
            _searchState.value = SearchUiState.Idle
        }
    }

    fun search(query: String) {
        if (query.isBlank()) { _searchState.value = SearchUiState.Idle; return }
        val apiKey = (_apiKeyState.value as? ApiKeyState.Present)?.key ?: return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searchState.value = SearchUiState.Loading
            runCatching { repository.search(query, apiKey) }
                .onSuccess { _searchState.value = SearchUiState.Results(it) }
                .onFailure { _searchState.value = SearchUiState.Error(it.message ?: "Error al buscar") }
        }
    }

    fun downloadVideo(item: YoutubeSearchResult.Video) {
        val current = stateOf(item.videoId)
        if (current != ItemDownloadState.NONE && current != ItemDownloadState.ERROR) return
        viewModelScope.launch {
            setVideoState(item.videoId, ItemDownloadState.FETCHING)
            runCatching { repository.getAudioStreamUrl(item.videoId) }
                .onFailure {
                    setVideoState(item.videoId, ItemDownloadState.ERROR)
                }
                .onSuccess { streamUrl ->
                    setVideoState(item.videoId, ItemDownloadState.DOWNLOADING)
                    runCatching {
                        downloadChunked(
                            url          = streamUrl,
                            fileName     = sanitize(item.title) + ".m4a",
                            title        = item.title,
                            artist       = item.channelName,
                            album        = item.channelName,
                            thumbnailUrl = item.thumbnailUrl
                        )
                    }
                        .onSuccess { setVideoState(item.videoId, ItemDownloadState.DONE) }
                        .onFailure {
                            setVideoState(item.videoId, ItemDownloadState.ERROR)
                        }
                }
        }
    }

    fun downloadPlaylist(playlist: YoutubeSearchResult.Playlist) {
        val current = _playlistStates.value[playlist.playlistId]
        if (current == ItemDownloadState.FETCHING || current == ItemDownloadState.DOWNLOADING) return
        val apiKey = (_apiKeyState.value as? ApiKeyState.Present)?.key ?: return

        viewModelScope.launch {
            setPlaylistState(playlist.playlistId, ItemDownloadState.FETCHING)

            val videos = runCatching {
                repository.getPlaylistVideos(playlist.playlistId, apiKey)
            }.getOrElse {
                setPlaylistState(playlist.playlistId, ItemDownloadState.ERROR)
                return@launch
            }

            if (videos.isEmpty()) {
                setPlaylistState(playlist.playlistId, ItemDownloadState.ERROR)
                return@launch
            }

            setPlaylistState(playlist.playlistId, ItemDownloadState.DOWNLOADING)
            videos.forEach { setVideoState(it.videoId, ItemDownloadState.FETCHING) }

            videos.chunked(3).forEach { chunk ->
                chunk.map { video ->
                    async {
                        runCatching { repository.getAudioStreamUrl(video.videoId) }
                            .onFailure { setVideoState(video.videoId, ItemDownloadState.ERROR) }
                            .onSuccess { streamUrl ->
                                setVideoState(video.videoId, ItemDownloadState.DOWNLOADING)
                                runCatching {
                                    downloadChunked(
                                        url          = streamUrl,
                                        fileName     = sanitize(video.title) + ".m4a",
                                        title        = video.title,
                                        artist       = video.channelName,
                                        album        = playlist.title,
                                        thumbnailUrl = video.thumbnailUrl
                                    )
                                }
                                    .onSuccess { setVideoState(video.videoId, ItemDownloadState.DONE) }
                                    .onFailure { setVideoState(video.videoId, ItemDownloadState.ERROR) }
                            }
                    }
                }.awaitAll()
            }

            val allDone = videos.all { stateOf(it.videoId) == ItemDownloadState.DONE }
            setPlaylistState(
                playlist.playlistId,
                if (allDone) ItemDownloadState.DONE else ItemDownloadState.ERROR
            )
        }
    }

    private suspend fun downloadChunked(
        url          : String,
        fileName     : String,
        title        : String,
        artist       : String,
        album        : String,
        thumbnailUrl : String
    ) = withContext(Dispatchers.IO) {
        val musicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "Vora"
        ).also { it.mkdirs() }
        val outFile = File(musicDir, fileName)

        val totalSize = getContentLength(url)

        if (totalSize <= 0L) {
            downloadSimple(url, outFile)
        } else {
            RandomAccessFile(outFile, "rw").use { it.setLength(totalSize) }
            val chunkSize = totalSize / CHUNK_COUNT
            (0 until CHUNK_COUNT).map { i ->
                val start = i * chunkSize
                val end   = if (i == CHUNK_COUNT - 1) totalSize - 1 else start + chunkSize - 1
                async { downloadRange(url, outFile, start, end) }
            }.awaitAll()
        }

        writeMetadata(outFile, title, artist, album, thumbnailUrl)

        MediaScannerConnection.scanFile(
            getApplication(),
            arrayOf(outFile.absolutePath),
            arrayOf("audio/mp4"),
            null
        )
        _refreshLibrary.value++
    }

    private fun writeMetadata(
        file         : File,
        title        : String,
        artist       : String,
        album        : String,
        thumbnailUrl : String
    ) {
        try {
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tagOrCreateAndSetDefault as Mp4Tag

            tag.setField(FieldKey.TITLE,  title)
            tag.setField(FieldKey.ARTIST, artist)
            tag.setField(FieldKey.ALBUM,  album)

            runCatching {
                val conn = URL(thumbnailUrl).openConnection() as HttpURLConnection
                conn.connectTimeout = 10_000
                conn.readTimeout    = 10_000
                val imageBytes = conn.inputStream.readBytes()
                conn.disconnect()
                val artwork = ArtworkFactory.getNew()
                artwork.binaryData = imageBytes
                artwork.mimeType   = "image/jpeg"
                tag.setField(artwork)
            }

            audioFile.commit()
        } catch (e: Exception) {
            android.util.Log.w("VORA_DL", "Metadata could not be written: ${e.message}")
        }
    }

    private fun getContentLength(url: String): Long {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod  = "HEAD"
        conn.connectTimeout = 10_000
        conn.readTimeout    = 10_000
        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
        return try {
            conn.connect()
            conn.contentLengthLong
        } finally {
            conn.disconnect()
        }
    }

    private fun downloadRange(url: String, file: File, start: Long, end: Long) {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 30_000
        conn.readTimeout    = 30_000
        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
        conn.setRequestProperty("Range", "bytes=$start-$end")
        try {
            val bytes = conn.inputStream.readBytes()
            RandomAccessFile(file, "rw").use { raf ->
                raf.seek(start)
                raf.write(bytes)
            }
        } finally {
            conn.disconnect()
        }
    }

    private fun downloadSimple(url: String, file: File) {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 30_000
        conn.readTimeout    = 60_000
        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
        try {
            file.outputStream().use { out -> conn.inputStream.copyTo(out) }
        } finally {
            conn.disconnect()
        }
    }

    private fun setVideoState(videoId: String, state: ItemDownloadState) {
        _itemStates.value = _itemStates.value.toMutableMap().also { it[videoId] = state }
    }

    private fun setPlaylistState(playlistId: String, state: ItemDownloadState) {
        _playlistStates.value = _playlistStates.value.toMutableMap().also { it[playlistId] = state }
    }

    fun stateOf(videoId: String): ItemDownloadState =
        _itemStates.value[videoId] ?: ItemDownloadState.NONE

    fun playlistStateOf(playlistId: String): ItemDownloadState =
        _playlistStates.value[playlistId] ?: ItemDownloadState.NONE

    private fun sanitize(name: String): String =
        name.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(100)
}