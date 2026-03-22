package com.peedrovzxf.vora.data.model

data class Artist(
    val name: String,
    val albums: List<Album>,
    val songs: List<Song>
)