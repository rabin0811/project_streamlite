package com.streamlite.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.streamlite.app.data.local.dao.MediaDao
import com.streamlite.app.data.local.dao.PlaybackDao
import com.streamlite.app.data.local.dao.PlaylistDao
import com.streamlite.app.data.local.entity.MediaItemEntity
import com.streamlite.app.data.local.entity.PlaybackStateEntity
import com.streamlite.app.data.local.entity.PlaylistEntity
import com.streamlite.app.data.local.entity.PlaylistItemEntity

@Database(
    entities = [
        MediaItemEntity::class,
        PlaybackStateEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MediaDatabase : RoomDatabase() {

    abstract fun mediaDao(): MediaDao
    abstract fun playbackDao(): PlaybackDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: MediaDatabase? = null

        fun getInstance(context: Context): MediaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MediaDatabase::class.java,
                    "streamlite_media.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
