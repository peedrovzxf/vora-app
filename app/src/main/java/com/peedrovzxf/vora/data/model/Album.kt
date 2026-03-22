package com.peedrovzxf.vora.data.model

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val albumArtUri: String?,
    val songs: List<Song>
)