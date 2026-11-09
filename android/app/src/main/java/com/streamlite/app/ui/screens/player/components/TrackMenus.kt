package com.streamlite.app.ui.screens.player.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.streamlite.app.ui.theme.PremiumRed
import com.streamlite.app.player.StreamPlayer

@Composable
fun TrackSelectionMenu(
    videoTracks: List<StreamPlayer.VideoTrackInfo>,
    audioTracks: List<StreamPlayer.AudioTrackInfo>,
    subtitleTracks: List<StreamPlayer.SubtitleTrackInfo>,
    onSelectVideo: (Int) -> Unit,
    onSelectAudio: (Int) -> Unit,
    onSelectSubtitle: (Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp),
        color = Color.Black.copy(0.9f),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Tracks",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
            
            TrackSection("Video", videoTracks.map { it.id to it.label }, -2, onSelectVideo)
            TrackSection("Audio", audioTracks.map { it.id to it.label }, -2, onSelectAudio)
            TrackSection("Subtitles", subtitleTracks.map { it.id to it.label }, -2, onSelectSubtitle)
        }
    }
}

@Composable
private fun TrackSection(
    title: String,
    tracks: List<Pair<Int, String>>,
    selectedId: Int,
    onSelect: (Int) -> Unit
) {
    if (tracks.isNotEmpty()) {
        Text(
            text = title,
            color = PremiumRed,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        tracks.forEach { (id, label) ->
            Text(
                text = label,
                color = if (id == selectedId) PremiumRed else Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(id) }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Divider(color = Color.White.copy(0.1f), modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun SpeedSelectionMenu(
    currentSpeed: Float,
    onSelectSpeed: (Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(200.dp),
        color = Color.Black.copy(0.9f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Speed",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
            listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                Text(
                    text = "${speed}x",
                    color = if (speed == currentSpeed) PremiumRed else Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSpeed(speed) }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
