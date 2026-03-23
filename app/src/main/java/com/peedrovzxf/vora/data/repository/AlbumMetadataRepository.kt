package com.peedrovzxf.vora.data.repository

import com.peedrovzxf.vora.data.local.AlbumMetadataDao
import com.peedrovzxf.vora.data.local.AlbumMetadataEntity
import com.peedrovzxf.vora.data.local.AlbumSongOrderEntity
import com.peedrovzxf.vora.data.model.Album
import com.peedrovzxf.vora.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

class AlbumMetadataRepository(private val dao: AlbumMetadataDao) {

    fun getAlbumWithMetadata(album: Album): Flow<Album> {
        return combine(
            dao.getAlbumMetadata(album.name),
            dao.getSongOrder(album.name)
        ) { metadata, songOrder ->
            val sortedSongs = if (songOrder.isEmpty()) {
                album.songs.sortedBy { it.title }
            } else {
                val orderMap = songOrder.associate { it.songId to it.position }
                album.songs.sortedWith(compareBy(
                    { orderMap[it.id] ?: Int.MAX_VALUE },
                    { it.title }
                ))
            }
            album.copy(
                albumArtUri = metadata?.customAlbumArtPath ?: album.albumArtUri,
                name        = metadata?.customAlbumName ?: album.name,
                songs = sortedSongs
            )
        }
    }

    fun getAllAlbumsWithMetadata(albums: List<Album>): Flow<List<Album>> {
        return combine(
            dao.getAllAlbumMetadata(),
            flowOf(albums)
        ) { allMetadata, originalAlbums ->
            originalAlbums.map { album ->
                val metadata = allMetadata.find { it.albumName == album.name }
                album.copy(
                    albumArtUri = metadata?.customAlbumArtPath ?: album.albumArtUri,
                    name        = metadata?.customAlbumName ?: album.name
                )
            }
        }
    }

    suspend fun saveAlbumArt(albumName: String, artPath: String) {
        dao.upsertAlbumMetadata(
            AlbumMetadataEntity(
                albumName = albumName,
                customAlbumArtPath = artPath
            )
        )
    }

    suspend fun saveSongOrder(albumName: String, songs: List<Song>) {
        dao.clearSongOrder(albumName)
        songs.forEachIndexed { index, song ->
            dao.upsertSongOrder(
                AlbumSongOrderEntity(
                    songId = song.id,
                    albumName = albumName,
                    position = index
                )
            )
        }
    }

    suspend fun saveAlbumName(albumName: String, newName: String) {
        val existing = dao.getAlbumMetadataOnce(albumName)
        dao.upsertAlbumMetadata(
            AlbumMetadataEntity(
                albumName          = albumName,
                customAlbumArtPath = existing?.customAlbumArtPath,
                customAlbumName    = newName.ifBlank { null }
            )
        )
    }
}