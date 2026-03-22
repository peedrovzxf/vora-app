package com.peedrovzxf.vora.ui.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peedrovzxf.vora.data.local.AppDatabase
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.data.repository.SongMetadataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditSongViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SongMetadataRepository(
        AppDatabase.getInstance(application).songMetadataDao()
    )

    private val _song = MutableStateFlow<Song?>(null)
    val song: StateFlow<Song?> = _song

    fun setSong(song: Song) {
        _song.value = song
    }

    fun saveMetadata(
        songId: Long,
        title: String,
        artist: String,
        album: String,
        albumArtPath: String?
    ) {
        viewModelScope.launch {
            repository.saveMetadata(
                songId = songId,
                title = title.ifBlank { null },
                artist = artist.ifBlank { null },
                album = album.ifBlank { null },
                albumArtPath = albumArtPath
            )
        }
    }

    fun clearMetadata(songId: Long) {
        viewModelScope.launch {
            repository.clearMetadata(songId)
        }
    }
}