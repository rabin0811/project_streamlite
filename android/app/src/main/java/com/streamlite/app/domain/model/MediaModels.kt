package com.streamlite.app.domain.model

enum class MediaType {
    VIDEO, IMAGE, AUDIO, UNKNOWN
}

enum class VideoQuality(val label: String) {
    P240("240p"),
    P360("360p"),
    P480("480p"),
    P720("720p"),
    P1080("1080p"),
    P1440("1440p"),
    P2160("2160p"),
    P4320("4320p"),
    AUTO("Auto")
}

enum class SortOrder {
    DATE_MODIFIED,
    NAME,
    SIZE,
    DURATION
}

data class MediaItem(
    val id: String,
    val name: String,
    val path: String,
    val uri: String,
    val mimeType: String,
    val mediaType: MediaType,
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
) {
    val durationText: String
        get() {
            if (duration <= 0) return "00:00"
            val hours = duration / 3600000
            val minutes = (duration % 3600000) / 60000
            val seconds = (duration % 60000) / 1000
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

    val sizeText: String
        get() {
            val kb = size / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.1f GB", gb)
                mb >= 1.0 -> String.format("%.1f MB", mb)
                else -> String.format("%.1f KB", kb)
            }
        }

    val resolutionText: String
        get() = if (width > 0 && height > 0) "${width}x${height}" else "Unknown"
}

data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val itemCount: Int = 0
)

data class PlaybackPosition(
    val mediaId: String,
    val position: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val lastPlayedAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)

data class NetworkStream(
    val url: String,
    val name: String = url.substringAfterLast("/").substringBefore("?"),
    val type: StreamType = StreamType.UNKNOWN
)

data class PlaylistEntry(
    val name: String,
    val url: String,
    val logo: String? = null,
    val groupTitle: String? = null,
    val channelNumber: String? = null,
    val tvgId: String? = null
)

enum class StreamType {
    HLS, DASH, RTSP, FTP, SMB, WEBDAV, HTTP, UDP, RTP, UNKNOWN
}
