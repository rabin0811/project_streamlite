package com.streamlite.app.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.streamlite.app.domain.model.MediaItem
import com.streamlite.app.domain.model.MediaType
import com.streamlite.app.ui.navigation.NavRoutes
import com.streamlite.app.ui.theme.ProgressColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Library") })
        }
    ) { padding ->
        when (selectedTab) {
            0 -> LibraryOverviewTab(uiState, navController, onTabSwitch = { selectedTab = it })
            1 -> MediaGridTab(
                title = "Videos",
                mediaList = uiState.videos,
                navController = navController,
                onBack = { selectedTab = 0 }
            )
            2 -> MediaGridTab(
                title = "Images",
                mediaList = uiState.images,
                navController = navController,
                onBack = { selectedTab = 0 }
            )
        }
    }
}

@Composable
fun LibraryOverviewTab(
    uiState: LibraryUiState,
    navController: NavController,
    onTabSwitch: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            LibrarySection(
                title = "Videos",
                icon = Icons.Default.VideoLibrary,
                count = uiState.videos.size,
                onClick = { onTabSwitch(1) }
            )
        }
        if (uiState.videos.isNotEmpty()) {
            items(uiState.videos.take(5)) { media ->
                MediaListItem(media = media, onClick = {
                    navController.navigate(NavRoutes.playerRoute(media.id))
                })
            }
            item {
                Text(
                    "View all videos (${uiState.videos.size})",
                    color = ProgressColor,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp).clickable { onTabSwitch(1) }
                )
            }
            }

        item { LibrarySpacer() }

        item {
            LibrarySection(
                title = "Images",
                icon = Icons.Default.Image,
                count = uiState.images.size,
                onClick = { onTabSwitch(2) }
            )
        }
        if (uiState.images.isNotEmpty()) {
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.images.take(5)) { media ->
                        ImageThumbnail(media = media, onClick = {
                            navController.navigate(NavRoutes.imageViewerRoute(media.id))
                        })
                    }
                }
            }
            item {
                Text(
                    "View all images (${uiState.images.size})",
                    color = ProgressColor,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp).clickable { onTabSwitch(2) }
                )
            }
            }

        item { LibrarySpacer() }

        item {
            LibrarySection(
                title = "Favorites",
                icon = Icons.Default.Favorite,
                count = uiState.favorites.size,
                onClick = { navController.navigate(NavRoutes.FAVORITES) }
            )
        }

        item {
            LibrarySection(
                title = "Recently Played",
                icon = Icons.Default.History,
                count = uiState.recentlyPlayed.size,
                onClick = { navController.navigate(NavRoutes.RECENTLY_PLAYED) }
            )
        }
    }
}

@Composable
fun LibrarySpacer() {
    Spacer(Modifier.height(8.dp))
}

@Composable
fun LibrarySection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = ProgressColor, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
            Text(text = count.toString(), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaGridTab(
    title: String,
    mediaList: List<MediaItem>,
    navController: NavController,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (mediaList.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No $title found")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(120.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(mediaList) { media ->
                    MediaGridItem(media = media, onClick = {
                        when (media.mediaType) {
                            MediaType.VIDEO -> navController.navigate(NavRoutes.playerRoute(media.id))
                            MediaType.IMAGE -> navController.navigate(NavRoutes.imageViewerRoute(media.id))
                            else -> {}
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun MediaListItem(media: MediaItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(media.uri).crossfade(true).build(),
                contentDescription = media.name,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(text = media.name, style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (media.mediaType == MediaType.VIDEO && media.duration > 0) {
                    Text(text = media.durationText, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = ProgressColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    folderPath: String,
    navController: NavController,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val folderName = folderPath.substringAfterLast("/").ifBlank { folderPath }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(folderName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val mediaInFolder = uiState.videos.filter { it.folderPath == folderPath } +
                uiState.images.filter { it.folderPath == folderPath }

        if (mediaInFolder.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No media found in this folder")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(120.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(mediaInFolder) { media ->
                    MediaGridItem(media = media, onClick = {
                        when (media.mediaType) {
                            MediaType.VIDEO -> navController.navigate(NavRoutes.playerRoute(media.id))
                            MediaType.IMAGE -> navController.navigate(NavRoutes.imageViewerRoute(media.id))
                            else -> {}
                        }
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.favorites.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No favorites yet")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(120.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(uiState.favorites) { media ->
                    MediaGridItem(media = media, onClick = {
                        when (media.mediaType) {
                            MediaType.VIDEO -> navController.navigate(NavRoutes.playerRoute(media.id))
                            MediaType.IMAGE -> navController.navigate(NavRoutes.imageViewerRoute(media.id))
                            else -> {}
                        }
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyPlayedScreen(
    navController: NavController,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recently Played") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.recentlyPlayed.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No recently played media")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(120.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(uiState.recentlyPlayed) { media ->
                    MediaGridItem(media = media, onClick = {
                        when (media.mediaType) {
                            MediaType.VIDEO -> navController.navigate(NavRoutes.playerRoute(media.id))
                            MediaType.IMAGE -> navController.navigate(NavRoutes.imageViewerRoute(media.id))
                            else -> {}
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun MediaGridItem(media: MediaItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.padding(4.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(media.uri).crossfade(true).build(),
                contentDescription = media.name,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            if (media.mediaType == MediaType.VIDEO) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play",
                    tint = ProgressColor, modifier = Modifier.align(Alignment.Center).size(32.dp))
                Text(text = media.durationText, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp))
            }
        }
        Text(text = media.name, style = MaterialTheme.typography.labelSmall,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(4.dp))
    }
}

@Composable
fun ImageThumbnail(media: MediaItem, onClick: () -> Unit) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(media.uri).crossfade(true).build(),
        contentDescription = media.name,
        modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        contentScale = ContentScale.Crop
    )
}
