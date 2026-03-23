package com.peedrovzxf.vora.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumMetadataDao {

    @Query("SELECT * FROM album_metadata WHERE albumName = :albumName")
    fun getAlbumMetadata(albumName: String): Flow<AlbumMetadataEntity?>

    @Query("SELECT * FROM album_metadata")
    fun getAllAlbumMetadata(): Flow<List<AlbumMetadataEntity>>

    @Upsert
    suspend fun upsertAlbumMetadata(metadata: AlbumMetadataEntity)

    @Query("SELECT * FROM album_song_order WHERE albumName = :albumName ORDER BY position ASC")
    fun getSongOrder(albumName: String): Flow<List<AlbumSongOrderEntity>>

    @Upsert
    suspend fun upsertSongOrder(order: AlbumSongOrderEntity)

    @Query("DELETE FROM album_song_order WHERE albumName = :albumName")
    suspend fun clearSongOrder(albumName: String)

    @Query("SELECT * FROM album_metadata WHERE albumName = :albumName LIMIT 1")
    suspend fun getAlbumMetadataOnce(albumName: String): AlbumMetadataEntity?
}