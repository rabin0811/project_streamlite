package com.streamlite.app.domain.repository

import com.streamlite.app.domain.model.MediaItem
import com.streamlite.app.domain.model.MediaType
import com.streamlite.app.domain.model.PlaybackPosition
import com.streamlite.app.domain.model.Playlist
import com.streamlite.app.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getAllMedia(): Flow<List<MediaItem>>
    fun getMediaByType(type: MediaType): Flow<List<MediaItem>>
    fun getFavorites(): Flow<List<MediaItem>>
    fun getRecentlyPlayed(): Flow<List<MediaItem>>
    fun searchMedia(query: String): Flow<List<MediaItem>>
    fun getMediaByFolder(folderPath: String): Flow<List<MediaItem>>
    fun getAllFolders(): Flow<List<String>>
    suspend fun getMediaById(id: String): MediaItem?
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)
    suspend fun updateLastPlayed(id: String)
    suspend fun deleteMedia(id: String)
    suspend fun scanMedia()
    fun getMediaCountByType(type: MediaType): Flow<Int>
}

interface PlaybackRepository {
    fun getAllPlaybackStates(): Flow<List<PlaybackPosition>>
    suspend fun getPlaybackState(mediaId: String): PlaybackPosition?
    suspend fun savePlaybackState(state: PlaybackPosition)
    suspend fun updatePosition(mediaId: String, position: Long)
    suspend fun deletePlaybackState(mediaId: String)
}

interface PlaylistRepository {
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(playlistId: Long): Playlist?
    suspend fun createPlaylist(name: String, description: String = ""): Long
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun deletePlaylist(playlistId: Long)
    fun getPlaylistItems(playlistId: Long): Flow<List<MediaItem>>
    suspend fun addToPlaylist(playlistId: Long, mediaId: String)
    suspend fun removeFromPlaylist(playlistId: Long, mediaId: String)
    suspend fun clearPlaylist(playlistId: Long)
}

interface PreferencesRepository {
    fun getThemeMode(): Flow<Int>
    suspend fun setThemeMode(mode: Int)
    fun getPlaybackQuality(): Flow<String>
    suspend fun setPlaybackQuality(quality: String)
    fun isHardwareAccelerationEnabled(): Flow<Boolean>
    suspend fun setHardwareAcceleration(enabled: Boolean)
    fun getDefaultSubtitleLanguage(): Flow<String>
    suspend fun setDefaultSubtitleLanguage(language: String)
    fun getCacheSize(): Flow<Long>
    suspend fun clearCache()
    fun getLanguage(): Flow<String>
    suspend fun setLanguage(language: String)
}
