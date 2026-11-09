package com.streamlite.app.ui.screens.image

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamlite.app.domain.model.MediaItem
import com.streamlite.app.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageViewerViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _mediaItem = MutableStateFlow<MediaItem?>(null)
    val mediaItem: StateFlow<MediaItem?> = _mediaItem.asStateFlow()

    fun loadMedia(mediaId: String) {
        viewModelScope.launch {
            _mediaItem.value = mediaRepository.getMediaById(mediaId)
        }
    }
}
