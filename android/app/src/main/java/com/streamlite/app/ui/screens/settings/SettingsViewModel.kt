package com.streamlite.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamlite.app.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val themeMode = preferencesRepository.getThemeMode().first()
            val quality = preferencesRepository.getPlaybackQuality().first()
            val hwAccel = preferencesRepository.isHardwareAccelerationEnabled().first()
            val subtitleLang = preferencesRepository.getDefaultSubtitleLanguage().first()
            val language = preferencesRepository.getLanguage().first()

            _uiState.value = SettingsUiState(
                themeMode = themeMode,
                playbackQuality = quality,
                hardwareAcceleration = hwAccel,
                subtitleLanguage = subtitleLang,
                language = language
            )
        }
    }

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
            _uiState.value = _uiState.value.copy(themeMode = mode)
        }
    }

    fun setPlaybackQuality(quality: String) {
        viewModelScope.launch {
            preferencesRepository.setPlaybackQuality(quality)
            _uiState.value = _uiState.value.copy(playbackQuality = quality)
        }
    }

    fun setHardwareAcceleration(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHardwareAcceleration(enabled)
            _uiState.value = _uiState.value.copy(hardwareAcceleration = enabled)
        }
    }

    fun setSubtitleLanguage(language: String) {
        viewModelScope.launch {
            preferencesRepository.setDefaultSubtitleLanguage(language)
            _uiState.value = _uiState.value.copy(subtitleLanguage = language)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(language)
            _uiState.value = _uiState.value.copy(language = language)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            preferencesRepository.clearCache()
        }
    }
}

data class SettingsUiState(
    val themeMode: Int = 0,
    val playbackQuality: String = "Auto",
    val hardwareAcceleration: Boolean = true,
    val subtitleLanguage: String = "en",
    val language: String = "en"
)
