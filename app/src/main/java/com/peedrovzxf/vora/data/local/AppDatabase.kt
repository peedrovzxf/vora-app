package com.peedrovzxf.vora.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        SongMetadataEntity::class,
        AlbumMetadataEntity::class,
        AlbumSongOrderEntity::class,
        PlayHistoryEntity::class,
        LikedSongEntity::class,
        AppSettingsEntity::class,
    ],
    version = 7
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun songMetadataDao(): SongMetadataDao
    abstract fun albumMetadataDao(): AlbumMetadataDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun likedSongDao(): LikedSongDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vora_db"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
