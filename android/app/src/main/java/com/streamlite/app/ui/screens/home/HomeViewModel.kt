package com.streamlite.app.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamlite.app.domain.model.MediaItem
import com.streamlite.app.domain.model.MediaType
import com.streamlite.app.domain.model.Playlist
import com.streamlite.app.domain.repository.MediaRepository
import com.streamlite.app.domain.repository.PlaylistRepository
import com.streamlite.app.ui.screens.network.SavedPlaylistData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaRepository: MediaRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadMedia()
        loadSavedPlaylists()
    }

    private fun loadSavedPlaylists() {
        val prefs = context.getSharedPreferences("playlists", Context.MODE_PRIVATE)
        val json = prefs.getString("saved_playlists", "[]") ?: "[]"
        val arr = JSONArray(json)
        val list = mutableListOf<SavedPlaylistData>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(SavedPlaylistData(
                name = obj.getString("name"),
                url = obj.getString("url"),
                channelCount = obj.getInt("channelCount")
            ))
        }
        _uiState.value = _uiState.value.copy(iptvPlaylists = list)
    }

    fun refreshSavedPlaylists() {
        loadSavedPlaylists()
    }

    private fun loadMedia() {
        viewModelScope.launch {
            combine(
                mediaRepository.getRecentlyPlayed(),
                mediaRepository.getMediaByType(MediaType.VIDEO),
                mediaRepository.getMediaByType(MediaType.IMAGE),
                playlistRepository.getAllPlaylists()
            ) { recent, videos, images, playlists ->
                HomeUiState(
                    recentMedia = recent.take(10),
                    videos = videos,
                    images = images,
                    playlists = playlists,
                    totalVideos = videos.size,
                    totalImages = images.size,
                    iptvPlaylists = _uiState.value.iptvPlaylists,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            loadMedia()
        } else {
            viewModelScope.launch {
                mediaRepository.searchMedia(query).collect { results ->
                    _uiState.value = _uiState.value.copy(searchResults = results, isSearching = true)
                }
            }
        }
    }

    fun clearSearch() { _searchQuery.value = ""; loadMedia() }

    fun toggleFavorite(mediaId: String, isFavorite: Boolean) {
        viewModelScope.launch { mediaRepository.toggleFavorite(mediaId, !isFavorite) }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch { playlistRepository.createPlaylist(name) }
    }

    fun scanMedia() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            mediaRepository.scanMedia()
            _uiState.value = _uiState.value.copy(isScanning = false)
        }
    }
}

data class HomeUiState(
    val recentMedia: List<MediaItem> = emptyList(),
    val videos: List<MediaItem> = emptyList(),
    val images: List<MediaItem> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val searchResults: List<MediaItem> = emptyList(),
    val totalVideos: Int = 0,
    val totalImages: Int = 0,
    val isLoading: Boolean = true,
    val isScanning: Boolean = false,
    val isSearching: Boolean = false,
    val iptvPlaylists: List<SavedPlaylistData> = emptyList()
)
