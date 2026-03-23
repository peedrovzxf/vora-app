package com.peedrovzxf.vora.ui.liked

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peedrovzxf.vora.data.local.AppDatabase
import com.peedrovzxf.vora.data.repository.LikedSongsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LikedSongsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LikedSongsRepository(
        AppDatabase.getInstance(application).likedSongDao()
    )

    val likedIds: StateFlow<Set<Long>> = repository.getAllLikedIds()
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5_000),
            initialValue  = emptySet()
        )

    val likedCount: StateFlow<Int> = repository.getLikedCount()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    fun toggle(songId: Long) {
        viewModelScope.launch {
            repository.toggle(songId, currentlyLiked = songId in likedIds.value)
        }
    }

    fun isLiked(songId: Long): Boolean = songId in likedIds.value
}
