package com.streamlite.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamlite.app.ui.theme.PremiumBlack
import com.streamlite.app.ui.theme.PremiumRed
import com.streamlite.app.ui.theme.PremiumWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("SETTINGS", 
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PremiumBlack,
                    titleContentColor = PremiumWhite
                )
            )
        },
        containerColor = PremiumBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            PremiumSettingsSection("Player Configuration") {
                PremiumSettingsItem(Icons.Default.Hd, "Video Quality", uiState.playbackQuality) { }
                PremiumSettingsToggle("Hardware Acceleration", "Use GPU for decoding", uiState.hardwareAcceleration) {
                    viewModel.setHardwareAcceleration(it)
                }
            }

            PremiumSettingsSection("Subtitle Customization") {
                PremiumSettingsItem(Icons.Default.Subtitles, "Default Language", uiState.subtitleLanguage.uppercase()) { }
            }

            PremiumSettingsSection("System & Storage") {
                PremiumSettingsItem(Icons.Default.DeleteSweep, "Clear Cache", "Free up storage space") { }
                PremiumSettingsItem(Icons.Default.Info, "Version", "1.0.0 (Premium Build)") { }
            }
        }
    }
}

@Composable
fun PremiumSettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(color = PremiumRed, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp), content = content)
        }
    }
}

@Composable
fun PremiumSettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PremiumWhite.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = PremiumWhite, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, color = PremiumWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PremiumWhite.copy(alpha = 0.3f))
    }
}

@Composable
fun PremiumSettingsToggle(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = PremiumWhite, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, color = PremiumWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = PremiumRed,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}
