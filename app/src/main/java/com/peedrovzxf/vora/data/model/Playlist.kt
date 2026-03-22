package com.peedrovzxf.vora.data.model

data class Playlist(
    val id: Long,
    val name: String,
    val songs: List<Song>
)