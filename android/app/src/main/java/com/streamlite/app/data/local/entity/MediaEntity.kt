package com.streamlite.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val path: String,
    val uri: String,
    val mimeType: String,
    val mediaType: String,
    val duration: Long = 0L,
    val size: Long = 0L,
    val dateModified: Long = 0L,
    val thumbnailPath: String? = null,
    val folderPath: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val isFavorite: Boolean = false,
    val lastPlayedAt: Long = 0L,
    val playCount: Int = 0
)

@Entity(tableName = "playback_states")
data class PlaybackStateEntity(
    @PrimaryKey
    val mediaId: String,
    val position: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val lastPlayedAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val itemCount: Int = 0
)

@Entity(
    tableName = "playlist_items",
    primaryKeys = ["playlistId", "mediaId", "position"]
)
data class PlaylistItemEntity(
    val playlistId: Long,
    val mediaId: String,
    val position: Int
)
