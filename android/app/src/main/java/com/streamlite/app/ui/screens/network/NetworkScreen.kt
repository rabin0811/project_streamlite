package com.streamlite.app.ui.screens.network

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.streamlite.app.ui.navigation.NavRoutes
import com.streamlite.app.ui.theme.PremiumBlack
import com.streamlite.app.ui.theme.PremiumBlue
import com.streamlite.app.ui.theme.PremiumRed
import com.streamlite.app.ui.theme.PremiumWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    navController: NavController,
    viewModel: NetworkViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddUrlDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("NETWORK STREAMS", 
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PremiumWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PremiumBlack,
                    titleContentColor = PremiumWhite
                )
            )
        },
        containerColor = PremiumBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddUrlDialog = true },
                containerColor = PremiumRed,
                contentColor = PremiumWhite
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    "Quick Connect",
                    style = MaterialTheme.typography.titleSmall,
                    color = PremiumWhite.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProtocolCard("HTTP/HLS", Icons.Default.Language, PremiumBlue, Modifier.weight(1f))
                    ProtocolCard("IPTV", Icons.Default.Tv, PremiumRed, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProtocolCard("SMB", Icons.Default.Computer, PremiumBlue, Modifier.weight(1f))
                    ProtocolCard("FTP", Icons.Default.Storage, PremiumRed, Modifier.weight(1f))
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Recent Streams",
                    style = MaterialTheme.typography.titleSmall,
                    color = PremiumWhite.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            items(uiState.streamHistory) { stream ->
                StreamItem(stream.name, stream.url) {
                    navController.navigate(NavRoutes.playerRoute(stream.url))
                }
            }
        }
    }

    if (showAddUrlDialog) {
        AddUrlDialog(
            url = uiState.urlInput,
            onUrlChange = { viewModel.onUrlInputChange(it) },
            onConfirm = {
                viewModel.addStream(uiState.urlInput)
                showAddUrlDialog = false
            },
            onDismiss = { showAddUrlDialog = false }
        )
    }
}

@Composable
fun ProtocolCard(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, color = PremiumWhite, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun StreamItem(name: String, url: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PremiumRed)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(name, color = PremiumWhite, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Text(url, color = PremiumWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun AddUrlDialog(
    url: String,
    onUrlChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = { Text("Stream URL", color = PremiumWhite) },
        text = {
            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://...", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PremiumWhite,
                    unfocusedTextColor = PremiumWhite,
                    focusedBorderColor = PremiumRed,
                    unfocusedBorderColor = Color.Gray
                )
            )
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = PremiumRed)) {
                Text("CONNECT")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = PremiumWhite)
            }
        }
    )
}
