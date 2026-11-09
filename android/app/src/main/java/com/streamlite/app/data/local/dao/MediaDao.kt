package com.streamlite.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.streamlite.app.data.local.entity.MediaItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

    @Query("SELECT * FROM media_items ORDER BY dateModified DESC")
    fun getAllMedia(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE mediaType = :type ORDER BY dateModified DESC")
    fun getMediaByType(type: String): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE isFavorite = 1 ORDER BY dateModified DESC")
    fun getFavorites(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE lastPlayedAt > 0 ORDER BY lastPlayedAt DESC")
    fun getRecentlyPlayed(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE name LIKE '%' || :query || '%' OR path LIKE '%' || :query || '%'")
    fun searchMedia(query: String): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE folderPath = :folderPath ORDER BY name ASC")
    fun getMediaByFolder(folderPath: String): Flow<List<MediaItemEntity>>

    @Query("SELECT DISTINCT folderPath FROM media_items WHERE folderPath IS NOT NULL ORDER BY folderPath ASC")
    fun getAllFolders(): Flow<List<String>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaById(id: String): MediaItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(item: MediaItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(items: List<MediaItemEntity>)

    @Update
    suspend fun updateMedia(item: MediaItemEntity)

    @Query("UPDATE media_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE media_items SET lastPlayedAt = :timestamp, playCount = playCount + 1 WHERE id = :id")
    suspend fun updateLastPlayed(id: String, timestamp: Long)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMedia(id: String)

    @Query("DELETE FROM media_items")
    suspend fun deleteAllMedia()

    @Query("SELECT COUNT(*) FROM media_items WHERE mediaType = :type")
    fun getMediaCountByType(type: String): Flow<Int>
}
