package com.peedrovzxf.vora.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peedrovzxf.vora.data.local.AppDatabase
import com.peedrovzxf.vora.data.model.Album
import com.peedrovzxf.vora.data.repository.AlbumMetadataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class AlbumDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AlbumMetadataRepository(
        AppDatabase.getInstance(application).albumMetadataDao()
    )

    private val _album = MutableStateFlow<Album?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val album: StateFlow<Album?> = _album
        .flatMapLatest { album ->
            if (album == null) flowOf(null)
            else repository.getAlbumWithMetadata(album).map { it }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun setAlbum(album: Album) {
        _album.value = album
    }
}