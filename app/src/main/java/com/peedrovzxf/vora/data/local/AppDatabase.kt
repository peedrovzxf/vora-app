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
    ],
    version = 5
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun songMetadataDao(): SongMetadataDao
    abstract fun albumMetadataDao(): AlbumMetadataDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun likedSongDao(): LikedSongDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `liked_songs` " +
                    "(`songId` INTEGER NOT NULL, PRIMARY KEY(`songId`))"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vora_db"
                )
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
