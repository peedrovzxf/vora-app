package com.peedrovzxf.vora.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SongMetadataDao {

    @Query("SELECT * FROM song_metadata WHERE songId = :songId")
    fun getMetadata(songId: Long): Flow<SongMetadataEntity?>

    @Query("SELECT * FROM song_metadata")
    fun getAllMetadata(): Flow<List<SongMetadataEntity>>

    @Upsert
    suspend fun upsertMetadata(metadata: SongMetadataEntity)

    @Query("DELETE FROM song_metadata WHERE songId = :songId")
    suspend fun deleteMetadata(songId: Long)
}