package com.streamlite.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.streamlite.app.domain.model.MediaItem
import com.streamlite.app.domain.model.MediaType
import com.streamlite.app.ui.navigation.NavRoutes
import com.streamlite.app.ui.theme.PremiumBlue
import com.streamlite.app.ui.theme.PremiumRed
import com.streamlite.app.ui.theme.PremiumBlack
import com.streamlite.app.ui.theme.PremiumWhite
import com.streamlite.app.ui.screens.network.SavedPlaylistData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("STREAM LITE", 
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = PremiumRed
                        )
                    ) 
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = PremiumWhite)
                    }
                    IconButton(onClick = { viewModel.scanMedia() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PremiumWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumBlack)
            )
        },
        containerColor = PremiumBlack
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { CategoryGrid(navController) }

            if (uiState.recentMedia.isNotEmpty()) {
                item { SectionHeader("Continue Watching") }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.recentMedia) { media ->
                            PremiumMediaCard(media = media, onClick = {
                                navController.navigate(NavRoutes.playerRoute(media.id))
                            })
                        }
                    }
                }
            }

            if (uiState.iptvPlaylists.isNotEmpty()) {
                item { SectionHeader("IPTV & Network Streams") }
                items(uiState.iptvPlaylists) { iptv ->
                    IptvPremiumItem(iptv) { navController.navigate(NavRoutes.NETWORK) }
                }
            }

            if (uiState.videos.isNotEmpty()) {
                item { SectionHeader("All Videos") }
                items(uiState.videos) { media ->
                    PremiumListItem(media = media, onClick = {
                        navController.navigate(NavRoutes.playerRoute(media.id))
                    })
                }
            }
        }
    }
}

@Composable
fun CategoryGrid(navController: NavController) {
    val categories = listOf(
        CategoryItem("Videos", Icons.Default.VideoLibrary, PremiumRed, NavRoutes.LIBRARY),
        CategoryItem("Network", Icons.Default.Language, PremiumBlue, NavRoutes.NETWORK),
        CategoryItem("Favorites", Icons.Default.Favorite, PremiumRed, NavRoutes.FAVORITES),
        CategoryItem("Settings", Icons.Default.Settings, PremiumBlue, NavRoutes.SETTINGS)
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categories.forEach { category ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1A1A1A))
                    .clickable { navController.navigate(category.route) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(category.icon, null, tint = category.color, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(category.label, color = PremiumWhite, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = PremiumWhite,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    )
}

@Composable
fun IptvPremiumItem(iptv: SavedPlaylistData, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(PremiumBlue.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Tv, null, tint = PremiumBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(iptv.name, color = PremiumWhite, style = MaterialTheme.typography.bodyMedium)
                Text("${iptv.channelCount} Channels", color = PremiumWhite.copy(0.5f), style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

@Composable
fun PremiumMediaCard(media: MediaItem, onClick: () -> Unit) {
    Column(Modifier.width(200.dp).clickable(onClick = onClick)) {
        Box(Modifier.fillMaxWidth().aspectRatio(16f/9f).clip(RoundedCornerShape(16.dp))) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(media.uri).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.3f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PlayCircle, null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }
        Text(media.name, color = PremiumWhite, modifier = Modifier.padding(top = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun PremiumListItem(media: MediaItem, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(media.uri).crossfade(true).build(),
            null,
            modifier = Modifier.size(80.dp, 50.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(media.name, color = PremiumWhite, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(media.durationText, color = PremiumWhite.copy(0.5f), style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Default.PlayArrow, null, tint = PremiumRed)
    }
}

data class CategoryItem(val label: String, val icon: ImageVector, val color: Color, val route: String)
