package com.peedrovzxf.vora.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "album_metadata")
data class AlbumMetadataEntity(
    @PrimaryKey
    val albumName: String,
    val customAlbumArtPath: String? = null
)