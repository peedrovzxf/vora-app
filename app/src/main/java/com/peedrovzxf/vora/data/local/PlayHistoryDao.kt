package com.peedrovzxf.vora.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayHistoryDao {

    @Insert
    suspend fun insertPlay(play: PlayHistoryEntity)

    @Query("SELECT * FROM play_history ORDER BY playedAt DESC LIMIT 50")
    fun getRecentPlays(): Flow<List<PlayHistoryEntity>>

    @Query("""
        SELECT songId, COUNT(*) as playCount 
        FROM play_history 
        GROUP BY songId 
        ORDER BY playCount DESC 
        LIMIT 20
    """)
    fun getMostPlayedSongIds(): Flow<List<SongPlayCount>>

    @Query("DELETE FROM play_history WHERE playedAt < :before")
    suspend fun clearOldHistory(before: Long)
}

data class SongPlayCount(
    val songId: Long,
    val playCount: Int
)