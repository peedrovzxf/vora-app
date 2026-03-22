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
import com.peedrovzxf.vora.data.repository.PlayHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerController(context: Context, private val historyRepository: PlayHistoryRepository) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isShuffled = MutableStateFlow(false)
    val isShuffled: StateFlow<Boolean> = _isShuffled

    private val _repeatMode = MutableStateFlow(RepeatMode.NONE)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode

    enum class RepeatMode { NONE, ONE, ALL }

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _queueState = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queueState

    private var _queueList: List<Song> = emptyList()
    private var originalQueue: List<Song> = emptyList()
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
                    if (playbackState == Player.STATE_ENDED) {
                        when (_repeatMode.value) {
                            RepeatMode.ONE -> {
                                controller?.seekTo(0)
                                controller?.play()
                            }
                            RepeatMode.ALL -> {
                                currentIndex = (currentIndex + 1) % _queueList.size
                                playCurrent()
                            }
                            RepeatMode.NONE -> {
                                if (currentIndex < _queueList.size - 1) {
                                    currentIndex++
                                    playCurrent()
                                }
                            }
                        }
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {}
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

    fun toggleShuffle() {
        _isShuffled.value = !_isShuffled.value
        if (_isShuffled.value) {
            val currentSong = _queueList[currentIndex]
            _queueList = _queueList.shuffled()
            currentIndex = _queueList.indexOf(currentSong)
        } else {
            val currentSong = _queueList[currentIndex]
            _queueList = originalQueue.toList()
            currentIndex = _queueList.indexOf(currentSong)
        }
        _queueState.value = _queueList
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.NONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.NONE
        }
        controller?.repeatMode = Player.REPEAT_MODE_OFF
    }

    fun play(song: Song, songs: List<Song>) {
        originalQueue = songs
        currentIndex = songs.indexOf(song)
        _queueList = if (_isShuffled.value) {
            val shuffled = songs.shuffled().toMutableList()
            shuffled.remove(song)
            shuffled.add(0, song)
            currentIndex = 0
            shuffled
        } else {
            songs
        }
        _queueState.value = _queueList
        playCurrent()
    }

    private fun playCurrent() {
        if (currentIndex < 0 || currentIndex >= _queueList.size) return
        val song = _queueList[currentIndex]
        _currentSong.value = song
        val mediaItem = song.toMediaItem()
        controller?.setMediaItem(mediaItem)
        controller?.prepare()
        controller?.play()
        scope.launch {
            historyRepository.recordPlay(song.id)
        }
    }

    fun togglePlayPause() {
        if (controller?.isPlaying == true) controller?.pause() else controller?.play()
    }

    fun next() {
        when {
            currentIndex < _queueList.size - 1 -> {
                currentIndex++
                playCurrent()
            }
            _repeatMode.value == RepeatMode.ALL -> {
                currentIndex = 0
                playCurrent()
            }
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

    fun playFromQueue(index: Int) {
        currentIndex = index
        playCurrent()
    }

    fun addToQueue(song: Song) {
        _queueList = _queueList.toMutableList().also { it.add(song) }
        _queueState.value = _queueList
    }

    fun moveInQueue(from: Int, to: Int) {
        if (from == to) return
        _queueList = _queueList.toMutableList().also { it.add(to, it.removeAt(from)) }
        when {
            from == currentIndex -> currentIndex = to
            currentIndex in (from + 1)..to -> currentIndex--
            currentIndex in to..<from -> currentIndex++
        }
        _queueState.value = _queueList
    }

    fun removeFromQueue(index: Int) {
        if (index == currentIndex) return
        _queueList = _queueList.toMutableList().also { it.removeAt(index) }
        if (index < currentIndex) currentIndex--
        _queueState.value = _queueList
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