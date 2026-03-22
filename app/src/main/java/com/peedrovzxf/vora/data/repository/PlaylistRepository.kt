package com.peedrovzxf.vora.data.repository

import com.peedrovzxf.vora.data.local.PlaylistDao
import com.peedrovzxf.vora.data.local.PlaylistEntity
import com.peedrovzxf.vora.data.local.PlaylistSongEntity
import com.peedrovzxf.vora.data.model.Playlist
import com.peedrovzxf.vora.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class PlaylistRepository(private val dao: PlaylistDao) {

    fun getPlaylists(allSongs: List<Song>): Flow<List<Playlist>> {
        return dao.getAllPlaylists().combine(
            kotlinx.coroutines.flow.flowOf(allSongs)
        ) { entities, songs ->
            entities.map { entity ->
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    songs = emptyList()
                )
            }
        }
    }

    suspend fun createPlaylist(name: String): Long {
        return dao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long, position: Int) {
        dao.insertPlaylistSong(
            PlaylistSongEntity(
                playlistId = playlistId,
                songId = songId,
                position = position
            )
        )
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        dao.removeSongFromPlaylist(playlistId, songId)
    }

    suspend fun deletePlaylist(id: Long, name: String) {
        dao.deletePlaylist(PlaylistEntity(id = id, name = name))
    }

    fun getPlaylistSongs(playlistId: Long, allSongs: List<Song>): Flow<List<Song>> {
        return dao.getSongsForPlaylist(playlistId).combine(
            kotlinx.coroutines.flow.flowOf(allSongs)
        ) { playlistSongs, songs ->
            playlistSongs.mapNotNull { ps ->
                songs.find { it.id == ps.songId }
            }
        }
    }
}