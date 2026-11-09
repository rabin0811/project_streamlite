package com.streamlite.app.ui.screens.player.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.streamlite.app.ui.theme.PremiumRed

@Composable
fun PlayerControlsContainer(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    title: String,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    playbackSpeed: Float,
    subtitlesEnabled: Boolean,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onPip: () -> Unit,
    onRotate: () -> Unit,
    onToggleSpeed: () -> Unit,
    onToggleTracks: () -> Unit,
    onToggleSubtitles: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            PlayerTopBar(
                title = title,
                onBack = onBack,
                onPip = onPip,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Center Play/Pause
            PlayPauseButton(
                isPlaying = isPlaying,
                onPlayPause = onPlayPause,
                modifier = Modifier.align(Alignment.Center)
            )

            // Bottom Controls
            PlayerBottomControls(
                currentPosition = currentPosition,
                duration = duration,
                playbackSpeed = playbackSpeed,
                subtitlesEnabled = subtitlesEnabled,
                onSeek = onSeek,
                onToggleSpeed = onToggleSpeed,
                onToggleTracks = onToggleTracks,
                onToggleSubtitles = onToggleSubtitles,
                onRotate = onRotate,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun PlayerTopBar(
    title: String,
    onBack: () -> Unit,
    onPip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Black.copy(0.7f), Color.Transparent)))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Text(
            text = title,
            color = Color.White,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = onPip) {
            Icon(Icons.Default.PictureInPicture, contentDescription = "PiP", tint = Color.White)
        }
    }
}

@Composable
private fun PlayPauseButton(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onPlayPause,
        modifier = modifier
            .size(72.dp)
            .background(Color.Black.copy(0.4f), CircleShape)
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun PlayerBottomControls(
    currentPosition: Long,
    duration: Long,
    playbackSpeed: Float,
    subtitlesEnabled: Boolean,
    onSeek: (Long) -> Unit,
    onToggleSpeed: () -> Unit,
    onToggleTracks: () -> Unit,
    onToggleSubtitles: () -> Unit,
    onRotate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.7f))))
            .padding(16.dp)
    ) {
        // Seek Bar
        Slider(
            value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
            onValueChange = { onSeek((it * duration).toLong()) },
            colors = SliderDefaults.colors(
                thumbColor = PremiumRed,
                activeTrackColor = PremiumRed,
                inactiveTrackColor = Color.White.copy(0.3f)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(currentPosition), color = Color.White, style = MaterialTheme.typography.labelSmall)
            Text(formatTime(duration), color = Color.White, style = MaterialTheme.typography.labelSmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onToggleSpeed) {
                Text("${playbackSpeed}x", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
            IconButton(onClick = onToggleTracks) {
                Icon(Icons.Default.Settings, null, tint = Color.White)
            }
            IconButton(onClick = onToggleSubtitles) {
                Icon(
                    imageVector = if (subtitlesEnabled) Icons.Default.Subtitles else Icons.Default.SubtitlesOff,
                    contentDescription = null,
                    tint = if (subtitlesEnabled) PremiumRed else Color.White
                )
            }
            IconButton(onClick = onRotate) {
                Icon(Icons.Default.ScreenRotation, null, tint = Color.White)
            }
            IconButton(onClick = { /* Lock logic could go here */ }) {
                Icon(Icons.Default.LockOpen, null, tint = Color.White)
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
