package com.peedrovzxf.vora.data.repository

import com.peedrovzxf.vora.data.local.LikedSongDao
import com.peedrovzxf.vora.data.local.LikedSongEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LikedSongsRepository(private val dao: LikedSongDao) {

    fun getAllLikedIds(): Flow<Set<Long>> =
        dao.getAllLikedIds().map { it.toSet() }

    fun isLiked(songId: Long): Flow<Boolean> =
        dao.isLiked(songId)

    fun getLikedCount(): Flow<Int> =
        dao.getLikedCount()

    suspend fun like(songId: Long) =
        dao.like(LikedSongEntity(songId))

    suspend fun unlike(songId: Long) =
        dao.unlike(songId)

    suspend fun toggle(songId: Long, currentlyLiked: Boolean) {
        if (currentlyLiked) unlike(songId) else like(songId)
    }
}
