package com.peedrovzxf.vora.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "liked_songs")
data class LikedSongEntity(
    @PrimaryKey val songId: Long
)
