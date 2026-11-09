package com.streamlite.app.ui.screens.player

import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.streamlite.app.ui.screens.player.components.PlayerControlsContainer
import com.streamlite.app.ui.screens.player.components.SpeedSelectionMenu
import com.streamlite.app.ui.screens.player.components.TrackSelectionMenu
import com.streamlite.app.ui.theme.PremiumRed
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    mediaId: String,
    navController: NavController,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val brightness by viewModel.brightness.collectAsState()
    val volume by viewModel.volume.collectAsState()

    val videoTracks by viewModel.availableVideoTracks.collectAsState()
    val audioTracks by viewModel.availableAudioTracks.collectAsState()
    val subtitleTracks by viewModel.availableSubtitleTracks.collectAsState()
    
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    
    var showControls by remember { mutableStateOf(true) }
    var seekIndicator by remember { mutableStateOf<String?>(null) }
    var showBrightnessIndicator by remember { mutableStateOf(false) }
    var showVolumeIndicator by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(brightness) {
        activity?.window?.attributes = activity?.window?.attributes?.apply {
            screenBrightness = brightness.coerceIn(0.01f, 1.0f)
        }
    }

    LaunchedEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    LaunchedEffect(isPlaying, showControls) {
        if (isPlaying && showControls && !uiState.isTrackMenuVisible && !uiState.isSpeedMenuVisible) {
            delay(3000)
            showControls = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { 
                        if (uiState.isSpeedMenuVisible || uiState.isTrackMenuVisible) {
                            if (uiState.isSpeedMenuVisible) viewModel.toggleSpeedMenu()
                            if (uiState.isTrackMenuVisible) viewModel.toggleTrackMenu()
                        } else {
                            showControls = !showControls 
                        }
                    },
                    onDoubleTap = { viewModel.togglePlayPause() }
                )
            }
            .pointerInput(duration) {
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = {
                        seekIndicator = null
                        showBrightnessIndicator = false
                        showVolumeIndicator = false
                    },
                    onDragCancel = {
                        seekIndicator = null
                        showBrightnessIndicator = false
                        showVolumeIndicator = false
                    },
                    onDrag = { change, dragAmount ->
                        if (!uiState.isScreenLocked) {
                            val width = size.width
                            val sensitivity = 0.002f
                            
                            if (kotlin.math.abs(dragAmount.x) > kotlin.math.abs(dragAmount.y)) {
                                if (duration > 0) {
                                    val seekDelta = (dragAmount.x / width * duration).toLong()
                                    viewModel.seekTo((currentPosition + seekDelta).coerceIn(0, duration))
                                    seekIndicator = if (seekDelta > 0) "+${formatTime(kotlin.math.abs(seekDelta))}" else "-${formatTime(kotlin.math.abs(seekDelta))}"
                                }
                            } else {
                                if (change.position.x < width / 2) {
                                    viewModel.adjustBrightness(-dragAmount.y * sensitivity)
                                    showBrightnessIndicator = true
                                } else {
                                    viewModel.adjustVolume(-dragAmount.y * sensitivity)
                                    showVolumeIndicator = true
                                }
                            }
                        }
                    }
                )
            }
    ) {
        // VLC Video Surface
        AndroidView(
            factory = { ctx ->
                SurfaceView(ctx).also { surfaceView ->
                    surfaceView.keepScreenOn = true
                    surfaceView.layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    val vout = viewModel.vlcPlayer.vlcVout
                    vout.setVideoView(surfaceView)
                    vout.attachViews()
                }
            },
            update = { surfaceView ->
                val vout = viewModel.vlcPlayer.vlcVout
                if (!vout.areViewsAttached()) {
                    vout.setVideoView(surfaceView)
                    vout.attachViews()
                }
            },
            onRelease = {
                viewModel.vlcPlayer.vlcVout.detachViews()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Indicators (Brightness, Volume, Seek)
        IndicatorOverlay(
            showBrightness = showBrightnessIndicator,
            brightness = brightness,
            showVolume = showVolumeIndicator,
            volume = volume,
            seekText = seekIndicator
        )

        // UI Controls Layer
        PlayerControlsContainer(
            isVisible = showControls && !uiState.isTrackMenuVisible && !uiState.isSpeedMenuVisible,
            title = uiState.mediaItem?.name ?: "Streaming...",
            isPlaying = isPlaying,
            currentPosition = currentPosition,
            duration = duration,
            playbackSpeed = playbackSpeed,
            subtitlesEnabled = uiState.subtitlesEnabled,
            onBack = { navController.popBackStack() },
            onPlayPause = { viewModel.togglePlayPause() },
            onSeek = { viewModel.seekTo(it) },
            onPip = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity?.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                }
            },
            onRotate = { 
                activity?.requestedOrientation = if (isLandscape) 
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT 
                else 
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE 
            },
            onToggleSpeed = { viewModel.toggleSpeedMenu() },
            onToggleTracks = { viewModel.toggleTrackMenu() },
            onToggleSubtitles = { viewModel.toggleSubtitles() }
        )

        // Menus (Tracks, Speed)
        if (uiState.isTrackMenuVisible) {
            TrackSelectionMenu(
                videoTracks = videoTracks,
                audioTracks = audioTracks,
                subtitleTracks = subtitleTracks,
                onSelectVideo = { viewModel.selectVideoTrack(it) },
                onSelectAudio = { viewModel.selectAudioTrack(it) },
                onSelectSubtitle = { viewModel.selectSubtitleTrack(it) },
                onClose = { viewModel.toggleTrackMenu() },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        if (uiState.isSpeedMenuVisible) {
            SpeedSelectionMenu(
                currentSpeed = playbackSpeed,
                onSelectSpeed = { viewModel.setPlaybackSpeed(it) },
                onClose = { viewModel.toggleSpeedMenu() },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun IndicatorOverlay(
    showBrightness: Boolean,
    brightness: Float,
    showVolume: Boolean,
    volume: Float,
    seekText: String?
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (showBrightness) {
            VerticalIndicator(
                icon = Icons.Default.Brightness6,
                value = brightness,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 32.dp)
            )
        }
        if (showVolume) {
            VerticalIndicator(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                value = volume,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 32.dp)
            )
        }
        seekText?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(0.6f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = it,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}

@Composable
private fun VerticalIndicator(icon: ImageVector, value: Float, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(44.dp)
            .height(160.dp)
            .background(Color.Black.copy(0.4f), RoundedCornerShape(22.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(value)
                    .background(PremiumRed)
                    .align(Alignment.BottomCenter)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

private fun formatTime(ms: Long): String {
    val s = ms / 1000
    return String.format("%02d:%02d", (s / 60) % 60, s % 60)
}
