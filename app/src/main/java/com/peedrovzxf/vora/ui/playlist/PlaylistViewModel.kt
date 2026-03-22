package com.peedrovzxf.vora.ui.playlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peedrovzxf.vora.data.local.AppDatabase
import com.peedrovzxf.vora.data.model.Playlist
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.data.repository.PlaylistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlaylistViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PlaylistRepository(
        AppDatabase.getInstance(application).playlistDao()
    )

    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())

    fun setSongs(songs: List<Song>) {
        _allSongs.value = songs
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlists: StateFlow<List<Playlist>> = _allSongs
        .flatMapLatest { songs ->
            repository.getPlaylists(songs)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            val currentSongs = repository.getPlaylistSongs(playlistId, _allSongs.value).first()
            repository.addSongToPlaylist(playlistId, songId, currentSongs.size)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun deletePlaylist(id: Long, name: String) {
        viewModelScope.launch {
            repository.deletePlaylist(id, name)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPlaylistSongs(playlistId: Long): StateFlow<List<Song>> {
        return _allSongs.flatMapLatest { songs ->
            repository.getPlaylistSongs(playlistId, songs)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
}