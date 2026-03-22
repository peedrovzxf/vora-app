package com.peedrovzxf.vora.data.local

import android.content.Context
import android.net.Uri
import java.io.File

object ImageStorage {

    fun saveAlbumArt(context: Context, uri: Uri, songId: Long): String {
        val dir = File(context.filesDir, "album_art")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "song_$songId.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    fun deleteAlbumArt(context: Context, songId: Long) {
        val file = File(context.filesDir, "album_art/song_$songId.jpg")
        if (file.exists()) file.delete()
    }
}