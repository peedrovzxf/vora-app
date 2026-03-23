package com.peedrovzxf.vora.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LikedSongDao {

    @Query("SELECT songId FROM liked_songs")
    fun getAllLikedIds(): Flow<List<Long>>

    @Query("SELECT COUNT(*) > 0 FROM liked_songs WHERE songId = :songId")
    fun isLiked(songId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun like(entity: LikedSongEntity)

    @Query("DELETE FROM liked_songs WHERE songId = :songId")
    suspend fun unlike(songId: Long)

    @Query("DELETE FROM liked_songs")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM liked_songs")
    fun getLikedCount(): Flow<Int>
}
