package com.peedrovzxf.vora.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_songs")
data class PlaylistSongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playlistId: Long,
    val songId: Long,
    val position: Int
)