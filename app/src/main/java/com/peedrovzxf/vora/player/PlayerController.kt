package com.peedrovzxf.vora.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class PlayerController(context: Context) {

    val player: ExoPlayer = ExoPlayer.Builder(context).build()

    fun play(path: String) {
        val mediaItem = MediaItem.fromUri(path)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun resume() {
        player.play()
    }

    fun release() {
        player.release()
    }
}