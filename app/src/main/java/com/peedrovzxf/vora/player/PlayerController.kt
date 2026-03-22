package com.peedrovzxf.vora.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.peedrovzxf.vora.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerController(context: Context) {

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

    private var controllerFuture: ListenableFuture<MediaController>
    private var controller: MediaController? = null

    init {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlayerService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            controller = controllerFuture.get()
            controller?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        _duration.value = controller?.duration ?: 0L
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    val index = controller?.currentMediaItemIndex ?: return
                    if (index in queue.indices) {
                        currentIndex = index
                        _currentSong.value = queue[index]
                    }
                }
            })
        }, MoreExecutors.directExecutor())

        scope.launch {
            while (true) {
                controller?.let {
                    if (it.isPlaying) {
                        val duration = it.duration
                        if (duration > 0) {
                            _progress.value = it.currentPosition.toFloat() / duration.toFloat()
                        }
                    }
                }
                delay(500)
            }
        }
    }

    fun play(song: Song, songs: List<Song>) {
        currentIndex = songs.indexOf(song)
        queue = songs

        val mediaItems = songs.map { it.toMediaItem() }
        controller?.setMediaItems(mediaItems, currentIndex, 0L)
        controller?.prepare()
        controller?.play()
        _currentSong.value = song
    }

    private fun playCurrent() {
        if (currentIndex < 0 || currentIndex >= queue.size) return
        val song = queue[currentIndex]
        _currentSong.value = song
        controller?.seekToDefaultPosition(currentIndex)
        controller?.play()
    }

    fun togglePlayPause() {
        if (controller?.isPlaying == true) controller?.pause() else controller?.play()
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
        val position = (progress * (controller?.duration ?: 0L)).toLong()
        controller?.seekTo(position)
    }

    fun release() {
        MediaController.releaseFuture(controllerFuture)
        scope.cancel()
    }

    private fun Song.toMediaItem(): MediaItem {
        val artworkUri = albumArtUri?.let { path ->
            if (path.startsWith("/")) {
                android.net.Uri.fromFile(java.io.File(path))
            } else {
                android.net.Uri.parse(path)
            }
        }

        val metadata = androidx.media3.common.MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setArtworkUri(artworkUri)
            .build()

        return MediaItem.Builder()
            .setUri(path)
            .setMediaMetadata(metadata)
            .build()
    }
}