package com.peedrovzxf.vora.data.repository

import com.peedrovzxf.vora.data.local.PlayHistoryDao
import com.peedrovzxf.vora.data.local.PlayHistoryEntity
import com.peedrovzxf.vora.data.model.Album
import com.peedrovzxf.vora.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlayHistoryRepository(private val dao: PlayHistoryDao) {

    suspend fun recordPlay(songId: Long) {
        dao.insertPlay(PlayHistoryEntity(songId = songId))

        val ninetyDaysAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
        dao.clearOldHistory(ninetyDaysAgo)
    }

    fun getRecentSongs(allSongs: List<Song>): Flow<List<Song>> {
        return dao.getRecentPlays().map { plays ->
            val seen = mutableSetOf<Long>()
            plays.mapNotNull { play ->
                if (seen.add(play.songId)) {
                    allSongs.find { it.id == play.songId }
                } else null
            }.take(20)
        }
    }

    fun getMostPlayedSongs(allSongs: List<Song>): Flow<List<Song>> {
        return dao.getMostPlayedSongIds().map { counts ->
            counts.mapNotNull { count ->
                allSongs.find { it.id == count.songId }
            }
        }
    }

    fun getFrequentArtists(allSongs: List<Song>): Flow<List<String>> {
        return dao.getMostPlayedSongIds().map { counts ->
            counts.mapNotNull { count ->
                allSongs.find { it.id == count.songId }?.artist
            }.distinct().take(10)
        }
    }

    fun getSuggestedAlbums(allSongs: List<Song>, allAlbums: List<Album>): Flow<List<Album>> {
        return dao.getMostPlayedSongIds().map { counts ->
            val frequentAlbums = counts.mapNotNull { count ->
                allSongs.find { it.id == count.songId }?.album
            }.distinct()

            val frequentArtists = counts.mapNotNull { count ->
                allSongs.find { it.id == count.songId }?.artist
            }.distinct()

            allAlbums.filter { album ->
                album.name in frequentAlbums || album.artist in frequentArtists
            }.take(10)
        }
    }
}