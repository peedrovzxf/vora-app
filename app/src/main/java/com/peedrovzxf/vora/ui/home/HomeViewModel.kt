package com.peedrovzxf.vora.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peedrovzxf.vora.data.local.AppDatabase
import com.peedrovzxf.vora.data.model.Album
import com.peedrovzxf.vora.data.model.Artist
import com.peedrovzxf.vora.data.model.Song
import com.peedrovzxf.vora.data.repository.PlayHistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PlayHistoryRepository(
        AppDatabase.getInstance(application).playHistoryDao()
    )

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    private val _artists = MutableStateFlow<List<Artist>>(emptyList())

    val allSongs: StateFlow<List<Song>> = _songs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setData(songs: List<Song>, albums: List<Album>, artists: List<Artist>) {
        _songs.value = songs
        _albums.value = albums
        _artists.value = artists
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentSongs: StateFlow<List<Song>> = _songs.flatMapLatest { songs ->
        repository.getRecentSongs(songs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val frequentArtists: StateFlow<List<String>> = _songs.flatMapLatest { songs ->
        repository.getFrequentArtists(songs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val suggestedAlbums: StateFlow<List<Album>> = combine(_songs, _albums) { songs, albums ->
        Pair(songs, albums)
    }.flatMapLatest { (songs, albums) ->
        repository.getSuggestedAlbums(songs, albums)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailySong: StateFlow<Song?> = _songs.map { songs ->
        if (songs.isEmpty()) null
        else {
            val dayIndex = (System.currentTimeMillis() / (1000 * 60 * 60 * 24)).toInt()
            songs[dayIndex % songs.size]
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}