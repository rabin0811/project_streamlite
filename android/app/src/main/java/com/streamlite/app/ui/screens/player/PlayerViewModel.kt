package com.streamlite.app.ui.screens.player

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.streamlite.app.domain.model.MediaItem
import com.streamlite.app.domain.model.MediaType
import com.streamlite.app.domain.model.PlaybackPosition
import com.streamlite.app.domain.repository.MediaRepository
import com.streamlite.app.domain.repository.PlaybackRepository
import com.streamlite.app.player.StreamPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val mediaRepository: MediaRepository,
    private val playbackRepository: PlaybackRepository,
    private val streamPlayer: StreamPlayer
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _brightness = MutableStateFlow(0.5f)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    private val _volume = MutableStateFlow(0.5f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val mediaId: String = savedStateHandle["mediaId"] ?: ""
    private var sleepTimerJob: Job? = null

    val vlcPlayer = streamPlayer.mediaPlayer
    val isPlaying = streamPlayer.isPlaying
    val currentPosition = streamPlayer.currentPosition
    val duration = streamPlayer.duration
    val bufferedPosition = streamPlayer.bufferedPosition
    val playbackSpeed = streamPlayer.playbackSpeed
    val playbackState = streamPlayer.playbackState
    val availableVideoTracks = streamPlayer.availableVideoTracks
    val availableAudioTracks = streamPlayer.availableAudioTracks
    val availableSubtitleTracks = streamPlayer.availableSubtitleTracks

    init {
        loadMedia()
    }

    private fun loadMedia() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val decodedMediaId = java.net.URLDecoder.decode(mediaId, "UTF-8")
                val isNetworkUrl = decodedMediaId.startsWith("http://") || decodedMediaId.startsWith("https://") ||
                        decodedMediaId.startsWith("rtsp://") || decodedMediaId.startsWith("smb://") ||
                        decodedMediaId.startsWith("ftp://") || decodedMediaId.startsWith("udp://") ||
                        decodedMediaId.contains(".m3u") || decodedMediaId.contains(".m3u8")

                if (isNetworkUrl) {
                    val name = decodedMediaId
                        .substringAfterLast("/").substringBefore("?").ifBlank { "Network Stream" }
                    val media = MediaItem(
                        id = decodedMediaId,
                        name = name,
                        path = decodedMediaId,
                        uri = decodedMediaId,
                        mimeType = "video/*",
                        mediaType = MediaType.VIDEO
                    )
                    _uiState.value = _uiState.value.copy(mediaItem = media)
                    streamPlayer.play(decodedMediaId, name)
                } else {
                    val media = mediaRepository.getMediaById(mediaId)
                    if (media != null) {
                        _uiState.value = _uiState.value.copy(mediaItem = media)
                        val savedPosition = playbackRepository.getPlaybackState(mediaId)
                        
                        val playUri = if (media.path.startsWith("/")) {
                            media.path
                        } else if (media.uri.startsWith("content://")) {
                            media.uri
                        } else {
                            media.uri
                        }
                        
                        streamPlayer.play(playUri, media.name)

                        if (savedPosition != null && !savedPosition.isCompleted) {
                            streamPlayer.seekTo(savedPosition.position)
                        }
                        mediaRepository.updateLastPlayed(mediaId)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun togglePlayPause() {
        streamPlayer.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        streamPlayer.seekTo(positionMs)
    }

    fun seekForward(seconds: Int = 10) {
        streamPlayer.seekForward(seconds)
    }

    fun seekBackward(seconds: Int = 10) {
        streamPlayer.seekBackward(seconds)
    }

    fun setPlaybackSpeed(speed: Float) {
        streamPlayer.setPlaybackSpeed(speed)
        _uiState.value = _uiState.value.copy(isSpeedMenuVisible = false)
    }

    fun selectVideoTrack(trackId: Int) {
        streamPlayer.selectVideoTrack(trackId)
    }

    fun selectAudioTrack(trackId: Int) {
        streamPlayer.selectAudioTrack(trackId)
    }

    fun selectSubtitleTrack(trackId: Int) {
        streamPlayer.selectSubtitleTrack(trackId)
    }

    fun toggleSubtitles() {
        val current = _uiState.value.subtitlesEnabled
        if (current) {
            streamPlayer.disableSubtitles()
        } else {
            streamPlayer.enableSubtitles()
        }
        _uiState.value = _uiState.value.copy(subtitlesEnabled = !current)
    }

    fun toggleLockScreen() {
        _uiState.value = _uiState.value.copy(isScreenLocked = !_uiState.value.isScreenLocked)
    }

    fun toggleSpeedMenu() {
        _uiState.value = _uiState.value.copy(
            isSpeedMenuVisible = !_uiState.value.isSpeedMenuVisible,
            isTrackMenuVisible = false
        )
    }

    fun toggleTrackMenu() {
        _uiState.value = _uiState.value.copy(
            isTrackMenuVisible = !_uiState.value.isTrackMenuVisible,
            isSpeedMenuVisible = false
        )
    }

    fun toggleControls() {
        if (!_uiState.value.isScreenLocked) {
            _uiState.value = _uiState.value.copy(showControls = !_uiState.value.showControls)
        }
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes > 0) {
            _uiState.value = _uiState.value.copy(sleepTimerMinutes = minutes)
            sleepTimerJob = viewModelScope.launch {
                delay(minutes * 60 * 1000L)
                streamPlayer.pause()
                _uiState.value = _uiState.value.copy(sleepTimerMinutes = 0)
            }
        } else {
            _uiState.value = _uiState.value.copy(sleepTimerMinutes = 0)
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(sleepTimerMinutes = 0)
    }

fun adjustBrightness(delta: Float) {
    _brightness.value = (_brightness.value + delta).coerceIn(0f, 1f)
}

fun adjustVolume(delta: Float) {
    val audioManager = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    val newVolume = (currentVolume + (delta * maxVolume).toInt()).coerceIn(0, maxVolume)
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
    _volume.value = newVolume.toFloat() / maxVolume
}

    fun toggleFavorite() {
        viewModelScope.launch {
            val media = _uiState.value.mediaItem ?: return@launch
            mediaRepository.toggleFavorite(media.id, media.isFavorite)
            _uiState.value = _uiState.value.copy(
                mediaItem = media.copy(isFavorite = !media.isFavorite)
            )
        }
    }

    fun savePlaybackPosition() {
        viewModelScope.launch {
            val pos = streamPlayer.currentPosition.value
            val dur = streamPlayer.duration.value
            playbackRepository.savePlaybackState(
                PlaybackPosition(
                    mediaId = mediaId,
                    position = pos,
                    duration = dur,
                    playbackSpeed = streamPlayer.playbackSpeed.value,
                    isCompleted = pos > 0 && dur > 0 && (dur - pos) < 5000
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        savePlaybackPosition()
        sleepTimerJob?.cancel()
    }
}

data class PlayerUiState(
    val mediaItem: MediaItem? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showControls: Boolean = true,
    val isSpeedMenuVisible: Boolean = false,
    val isTrackMenuVisible: Boolean = false,
    val isScreenLocked: Boolean = false,
    val subtitlesEnabled: Boolean = false,
    val sleepTimerMinutes: Int = 0
)
