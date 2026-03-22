package com.peedrovzxf.vora.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "album_song_order")
data class AlbumSongOrderEntity(
    @PrimaryKey
    val songId: Long,
    val albumName: String,
    val position: Int
)