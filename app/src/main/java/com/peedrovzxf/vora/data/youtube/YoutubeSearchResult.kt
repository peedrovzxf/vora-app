package com.peedrovzxf.vora.data.youtube

sealed class YoutubeSearchResult {

    data class Video(
        val videoId         : String,
        val title           : String,
        val channelName     : String,
        val thumbnailUrl    : String,
        val durationSeconds : Long
    ) : YoutubeSearchResult() {
        val formattedDuration: String get() {
            val h = durationSeconds / 3600
            val m = (durationSeconds % 3600) / 60
            val s = durationSeconds % 60
            return if (h > 0) "%d:%02d:%02d".format(h, m, s)
            else              "%d:%02d".format(m, s)
        }
    }

    data class Playlist(
        val playlistId   : String,
        val title        : String,
        val channelName  : String,
        val thumbnailUrl : String,
        val videoCount   : Int
    ) : YoutubeSearchResult()
}
