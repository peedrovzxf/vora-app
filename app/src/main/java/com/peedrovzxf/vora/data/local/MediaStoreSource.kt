package com.peedrovzxf.vora.data.local

import android.content.Context
import android.provider.MediaStore
import com.peedrovzxf.vora.data.model.Album
import com.peedrovzxf.vora.data.model.Song

class MediaStoreSource(private val context: Context) {

    fun getSongs(): List<Song> {
        val songs = mutableListOf<Song>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val albumId = cursor.getLong(albumIdCol)
                val albumArtUri = "content://media/external/audio/albumart/$albumId"

                songs.add(
                    Song(
                        id = cursor.getLong(idCol),
                        title = cursor.getString(titleCol) ?: "Unknown",
                        artist = cursor.getString(artistCol) ?: "Unknown",
                        album = cursor.getString(albumCol) ?: "Unknown",
                        duration = cursor.getLong(durationCol),
                        path = cursor.getString(dataCol),
                        albumArtUri = albumArtUri
                    )
                )
            }
        }

        return songs
    }

    fun getAlbums(): List<Album> {
        val songs = getSongs()
        return songs
            .groupBy { it.album }
            .map { (albumName, albumSongs) ->
                Album(
                    id = albumSongs.first().id,
                    name = albumName,
                    artist = albumSongs.first().artist,
                    albumArtUri = albumSongs.first().albumArtUri,
                    songs = albumSongs
                )
            }
            .sortedBy { it.name }
    }
}