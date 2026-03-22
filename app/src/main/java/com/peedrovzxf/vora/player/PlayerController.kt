package com.peedrovzxf.vora.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.peedrovzxf.vora.data.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerController(context: Context) {

    val player = ExoPlayer.Builder(context).build()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private var queue: List<Song> = emptyList()
    private var currentIndex: Int = -1

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.value = player.duration
                }
            }
        })

        scope.launch {
            while (true) {
                if (player.isPlaying) {
                    val duration = player.duration
                    if (duration > 0) {
                        _progress.value = player.currentPosition.toFloat() / duration.toFloat()
                    }
                }
                delay(500)
            }
        }
    }

    fun setQueue(songs: List<Song>, startIndex: Int) {
        queue = songs
        currentIndex = startIndex
        playCurrent()
    }

    fun play(song: Song, songs: List<Song>) {
        currentIndex = songs.indexOf(song)
        queue = songs
        playCurrent()
    }

    private fun playCurrent() {
        if (currentIndex < 0 || currentIndex >= queue.size) return
        val song = queue[currentIndex]
        _currentSong.value = song
        val mediaItem = MediaItem.fromUri(song.path)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun next() {
        if (currentIndex < queue.size - 1) {
            currentIndex++
            playCurrent()
        }
    }

    fun previous() {
        if (currentIndex > 0) {
            currentIndex--
            playCurrent()
        }
    }

    fun seekTo(progress: Float) {
        val position = (progress * player.duration).toLong()
        player.seekTo(position)
    }

    fun release() {
        scope.cancel()
        player.release()
    }
}