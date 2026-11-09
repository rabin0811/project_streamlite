package com.streamlite.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.streamlite.app.data.local.entity.PlaybackStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackDao {

    @Query("SELECT * FROM playback_states ORDER BY lastPlayedAt DESC")
    fun getAllPlaybackStates(): Flow<List<PlaybackStateEntity>>

    @Query("SELECT * FROM playback_states WHERE mediaId = :mediaId")
    suspend fun getPlaybackState(mediaId: String): PlaybackStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaybackState(state: PlaybackStateEntity)

    @Update
    suspend fun updatePlaybackState(state: PlaybackStateEntity)

    @Query("UPDATE playback_states SET position = :position WHERE mediaId = :mediaId")
    suspend fun updatePosition(mediaId: String, position: Long)

    @Query("DELETE FROM playback_states WHERE mediaId = :mediaId")
    suspend fun deletePlaybackState(mediaId: String)
}
