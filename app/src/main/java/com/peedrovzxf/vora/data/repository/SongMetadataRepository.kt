package com.peedrovzxf.vora.data.repository

import com.peedrovzxf.vora.data.local.SongMetadataDao
import com.peedrovzxf.vora.data.local.SongMetadataEntity
import com.peedrovzxf.vora.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SongMetadataRepository(private val dao: SongMetadataDao) {

    fun getSongWithMetadata(song: Song): Flow<Song> {
        return dao.getMetadata(song.id).combine(
            kotlinx.coroutines.flow.flowOf(song)
        ) { metadata, originalSong ->
            metadata?.let {
                originalSong.copy(
                    title = it.customTitle ?: originalSong.title,
                    artist = it.customArtist ?: originalSong.artist,
                    album = it.customAlbum ?: originalSong.album,
                    albumArtUri = it.customAlbumArtPath ?: originalSong.albumArtUri
                )
            } ?: originalSong
        }
    }

    fun getAllSongsWithMetadata(songs: List<Song>): Flow<List<Song>> {
        return dao.getAllMetadata().combine(
            kotlinx.coroutines.flow.flowOf(songs)
        ) { allMetadata, originalSongs ->
            originalSongs.map { song ->
                val metadata = allMetadata.find { it.songId == song.id }
                metadata?.let {
                    song.copy(
                        title = it.customTitle ?: song.title,
                        artist = it.customArtist ?: song.artist,
                        album = it.customAlbum ?: song.album,
                        albumArtUri = it.customAlbumArtPath ?: song.albumArtUri
                    )
                } ?: song
            }
        }
    }

    suspend fun saveMetadata(
        songId: Long,
        title: String?,
        artist: String?,
        album: String?,
        albumArtPath: String?
    ) {
        dao.upsertMetadata(
            SongMetadataEntity(
                songId = songId,
                customTitle = title,
                customArtist = artist,
                customAlbum = album,
                customAlbumArtPath = albumArtPath
            )
        )
    }

    suspend fun clearMetadata(songId: Long) {
        dao.deleteMetadata(songId)
    }

    suspend fun saveAlbumArtistOverride(songs: List<Song>, artist: String) {
        songs.forEach { song ->
            val existing = dao.getMetadataOnce(song.id)
            dao.upsertMetadata(
                SongMetadataEntity(
                    songId           = song.id,
                    customTitle      = existing?.customTitle,
                    customArtist     = artist.ifBlank { null },
                    customAlbum      = existing?.customAlbum,
                    customAlbumArtPath = existing?.customAlbumArtPath
                )
            )
        }
    }

    suspend fun saveAlbumOverrides(songs: List<Song>, artist: String, albumName: String) {
        songs.forEach { song ->
            val existing = dao.getMetadataOnce(song.id)
            dao.upsertMetadata(
                SongMetadataEntity(
                    songId             = song.id,
                    customTitle        = existing?.customTitle,
                    customArtist       = artist.ifBlank { null },
                    customAlbum        = albumName.ifBlank { null },
                    customAlbumArtPath = existing?.customAlbumArtPath
                )
            )
        }
    }
}