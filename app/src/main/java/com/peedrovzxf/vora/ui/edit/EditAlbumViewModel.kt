package com.peedrovzxf.vora.ui.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peedrovzxf.vora.data.local.AppDatabase
import com.peedrovzxf.vora.data.local.ImageStorage
import com.peedrovzxf.vora.data.model.Album
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.data.repository.AlbumMetadataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

class EditAlbumViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AlbumMetadataRepository(
        AppDatabase.getInstance(application).albumMetadataDao()
    )

    private val _album = MutableStateFlow<Album?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val album: StateFlow<Album?> get() = _album

    fun setAlbum(album: Album) {
        _album.value = album
    }

    fun saveAlbumArt(albumName: String, uri: android.net.Uri) {
        viewModelScope.launch {
            val path = ImageStorage.saveAlbumArt(
                getApplication(),
                uri,
                albumName.hashCode().toLong()
            )
            repository.saveAlbumArt(albumName, path)
        }
    }

    fun saveSongOrder(albumName: String, songs: List<Song>) {
        viewModelScope.launch {
            repository.saveSongOrder(albumName, songs)
        }
    }
}