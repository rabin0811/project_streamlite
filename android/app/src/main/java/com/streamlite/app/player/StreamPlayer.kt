package com.streamlite.app.player

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class StreamPlayer(context: Context) {

    private val libVlc: LibVLC
    val mediaPlayer: MediaPlayer
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionUpdateJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _bufferedPosition = MutableStateFlow(0L)
    val bufferedPosition: StateFlow<Long> = _bufferedPosition.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _playbackState = MutableStateFlow(0)
    val playbackState: StateFlow<Int> = _playbackState.asStateFlow()

    private val _availableVideoTracks = MutableStateFlow<List<VideoTrackInfo>>(emptyList())
    val availableVideoTracks: StateFlow<List<VideoTrackInfo>> = _availableVideoTracks.asStateFlow()

    private val _availableAudioTracks = MutableStateFlow<List<AudioTrackInfo>>(emptyList())
    val availableAudioTracks: StateFlow<List<AudioTrackInfo>> = _availableAudioTracks.asStateFlow()

    private val _availableSubtitleTracks = MutableStateFlow<List<SubtitleTrackInfo>>(emptyList())
    val availableSubtitleTracks: StateFlow<List<SubtitleTrackInfo>> = _availableSubtitleTracks.asStateFlow()

    init {
        val options = ArrayList<String>()
        options.add("--no-sub-autodetect-file")
        options.add("--swscale-mode=0")
        options.add("--network-caching=2000")
        options.add("--clock-jitter=0")
        options.add("--clock-synchro=0")
        options.add("--aout=opensles")
        options.add("-vvv")
        
        libVlc = LibVLC(context, options)
        mediaPlayer = MediaPlayer(libVlc)

        mediaPlayer.setEventListener { event ->
            when (event.type) {
                MediaPlayer.Event.Playing -> {
                    _isPlaying.value = true
                    _duration.value = mediaPlayer.length
                    startPositionUpdates()
                }
                MediaPlayer.Event.Paused, MediaPlayer.Event.Stopped, MediaPlayer.Event.EndReached -> {
                    _isPlaying.value = false
                    stopPositionUpdates()
                }
                MediaPlayer.Event.LengthChanged -> {
                    _duration.value = event.lengthChanged
                }
                MediaPlayer.Event.Vout -> {
                    parseTracks()
                }
            }
            _playbackState.value = event.type
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (true) {
                updatePositions()
                delay(500)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    private fun updatePositions() {
        _currentPosition.value = mediaPlayer.time
        _bufferedPosition.value = (mediaPlayer.position * mediaPlayer.length).toLong()
    }

    fun play(uriString: String, title: String = "") {
        val media = if (uriString.startsWith("/") || uriString.startsWith("file://")) {
            val path = uriString.removePrefix("file://")
            Media(libVlc, path)
        } else {
            Media(libVlc, Uri.parse(uriString))
        }
        
        media.addOption(":network-caching=2000")
        media.addOption(":file-caching=2000")
        media.addOption(":codec=all")

        mediaPlayer.media = media
        media.release()
        mediaPlayer.play()
    }

    fun pause() {
        mediaPlayer.pause()
    }

    fun resume() {
        mediaPlayer.play()
    }

    fun togglePlayPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer.time = positionMs
    }

    fun seekForward(seconds: Int = 10) {
        val newTime = mediaPlayer.time + seconds * 1000
        mediaPlayer.time = newTime.coerceAtMost(mediaPlayer.length)
    }

    fun seekBackward(seconds: Int = 10) {
        val newTime = mediaPlayer.time - seconds * 1000
        mediaPlayer.time = newTime.coerceAtLeast(0)
    }

    fun setPlaybackSpeed(speed: Float) {
        mediaPlayer.rate = speed
        _playbackSpeed.value = speed
    }

    fun selectVideoTrack(trackId: Int) {
        mediaPlayer.videoTrack = trackId
    }

    fun selectAudioTrack(trackId: Int) {
        mediaPlayer.audioTrack = trackId
    }

    fun selectSubtitleTrack(trackId: Int) {
        mediaPlayer.spuTrack = trackId
    }

    fun enableSubtitles() {
        val tracks = mediaPlayer.spuTracks
        if (!tracks.isNullOrEmpty()) {
            // Find the first non-disabled track if possible
            val firstTrack = tracks.firstOrNull { it.id != -1 } ?: tracks[0]
            mediaPlayer.spuTrack = firstTrack.id
        }
    }

    fun disableSubtitles() {
        mediaPlayer.spuTrack = -1
    }

    private fun parseTracks() {
        val videoTracks = mutableListOf<VideoTrackInfo>()
        val currentVideoTrack = mediaPlayer.videoTrack
        mediaPlayer.videoTracks?.forEachIndexed { index, track ->
            videoTracks.add(VideoTrackInfo(track.id, index, track.name, track.id == currentVideoTrack))
        }
        _availableVideoTracks.value = videoTracks

        val audioTracks = mutableListOf<AudioTrackInfo>()
        val currentAudioTrack = mediaPlayer.audioTrack
        mediaPlayer.audioTracks?.forEachIndexed { index, track ->
            audioTracks.add(AudioTrackInfo(track.id, index, track.name, track.id == currentAudioTrack))
        }
        _availableAudioTracks.value = audioTracks

        val subtitleTracks = mutableListOf<SubtitleTrackInfo>()
        val currentSubtitleTrack = mediaPlayer.spuTrack
        mediaPlayer.spuTracks?.forEachIndexed { index, track ->
            subtitleTracks.add(SubtitleTrackInfo(track.id, index, track.name, track.id == currentSubtitleTrack))
        }
        _availableSubtitleTracks.value = subtitleTracks
    }

    fun release() {
        stopPositionUpdates()
        mediaPlayer.stop()
        mediaPlayer.release()
        libVlc.release()
    }

    data class VideoTrackInfo(val id: Int, val index: Int, val label: String, val isSelected: Boolean)
    data class AudioTrackInfo(val id: Int, val index: Int, val label: String, val isSelected: Boolean)
    data class SubtitleTrackInfo(val id: Int, val index: Int, val label: String, val isSelected: Boolean)
}
