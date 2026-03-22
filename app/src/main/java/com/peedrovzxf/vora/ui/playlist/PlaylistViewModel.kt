package com.peedrovzxf.vora.ui.playlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peedrovzxf.vora.data.local.AppDatabase
import com.peedrovzxf.vora.data.model.Playlist
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.data.repository.PlaylistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PlaylistRepository(
        AppDatabase.getInstance(application).playlistDao()
    )

    private var allSongs: List<Song> = emptyList()

    fun setSongs(songs: List<Song>) {
        allSongs = songs
    }

    fun getPlaylists(): StateFlow<List<Playlist>> {
        return repository.getPlaylists(allSongs).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long, position: Int) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId, position)
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

    fun getPlaylistSongs(playlistId: Long): StateFlow<List<Song>> {
        return repository.getPlaylistSongs(playlistId, allSongs).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
}