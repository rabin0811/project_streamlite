package com.streamlite.app.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamlite.app.domain.model.MediaItem
import com.streamlite.app.domain.model.MediaType
import com.streamlite.app.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibrary()
    }

    private fun loadLibrary() {
        viewModelScope.launch {
            mediaRepository.getAllFolders().collect { folders ->
                _uiState.value = _uiState.value.copy(folders = folders)
            }
        }
        viewModelScope.launch {
            mediaRepository.getMediaByType(MediaType.VIDEO).collect { videos ->
                _uiState.value = _uiState.value.copy(videos = videos)
            }
        }
        viewModelScope.launch {
            mediaRepository.getMediaByType(MediaType.IMAGE).collect { images ->
                _uiState.value = _uiState.value.copy(images = images)
            }
        }
        viewModelScope.launch {
            mediaRepository.getFavorites().collect { favorites ->
                _uiState.value = _uiState.value.copy(favorites = favorites)
            }
        }
        viewModelScope.launch {
            mediaRepository.getRecentlyPlayed().collect { recent ->
                _uiState.value = _uiState.value.copy(recentlyPlayed = recent)
            }
        }
    }

    fun deleteMedia(mediaId: String) {
        viewModelScope.launch {
            mediaRepository.deleteMedia(mediaId)
        }
    }
}

data class LibraryUiState(
    val folders: List<String> = emptyList(),
    val videos: List<MediaItem> = emptyList(),
    val images: List<MediaItem> = emptyList(),
    val favorites: List<MediaItem> = emptyList(),
    val recentlyPlayed: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true
)
