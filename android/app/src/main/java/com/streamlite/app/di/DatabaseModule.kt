package com.streamlite.app.di

import android.content.Context
import com.streamlite.app.data.local.database.MediaDatabase
import com.streamlite.app.data.local.dao.MediaDao
import com.streamlite.app.data.local.dao.PlaybackDao
import com.streamlite.app.data.local.dao.PlaylistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MediaDatabase {
        return MediaDatabase.getInstance(context)
    }

    @Provides
    fun provideMediaDao(db: MediaDatabase): MediaDao = db.mediaDao()

    @Provides
    fun providePlaybackDao(db: MediaDatabase): PlaybackDao = db.playbackDao()

    @Provides
    fun providePlaylistDao(db: MediaDatabase): PlaylistDao = db.playlistDao()
}
