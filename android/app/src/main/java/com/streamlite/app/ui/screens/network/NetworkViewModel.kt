package com.streamlite.app.ui.screens.network

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamlite.app.domain.model.NetworkStream
import com.streamlite.app.domain.model.PlaylistEntry
import com.streamlite.app.domain.model.StreamType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class NetworkViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val _uiState = MutableStateFlow(NetworkUiState())
    val uiState: StateFlow<NetworkUiState> = _uiState.asStateFlow()

    private val cachedPlaylists = mutableMapOf<String, List<PlaylistEntry>>()
    private var savedPlaylistJsons = mutableMapOf<String, String>()

    init {
        loadSavedPlaylistData()
    }

    private fun loadSavedPlaylistData() {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("playlists", Context.MODE_PRIVATE)
            val json = prefs.getString("saved_playlists", "[]") ?: "[]"
            val arr = JSONArray(json)
            val loaded = mutableListOf<SavedPlaylistData>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                loaded.add(SavedPlaylistData(
                    name = obj.getString("name"),
                    url = obj.getString("url"),
                    channelCount = obj.getInt("channelCount")
                ))
                val entriesJson = prefs.getString("playlist_entries_${obj.getString("url")}", null)
                if (entriesJson != null) {
                    savedPlaylistJsons[obj.getString("url")] = entriesJson
                    cachedPlaylists[obj.getString("url")] = parseEntriesFromJson(entriesJson)
                }
            }
            _uiState.value = _uiState.value.copy(savedPlaylists = loaded)
        }
    }

    private fun persistPlaylists() {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("playlists", Context.MODE_PRIVATE)
            val arr = JSONArray()
            _uiState.value.savedPlaylists.forEach { p ->
                val obj = JSONObject()
                obj.put("name", p.name)
                obj.put("url", p.url)
                obj.put("channelCount", p.channelCount)
                arr.put(obj)
            }
            prefs.edit().putString("saved_playlists", arr.toString()).apply()
        }
    }

    private fun persistEntries(url: String, entries: List<PlaylistEntry>) {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("playlists", Context.MODE_PRIVATE)
            val arr = JSONArray()
            entries.forEach { e ->
                val obj = JSONObject()
                obj.put("name", e.name)
                obj.put("url", e.url)
                if (e.logo != null) obj.put("logo", e.logo)
                if (e.groupTitle != null) obj.put("groupTitle", e.groupTitle)
                if (e.channelNumber != null) obj.put("channelNumber", e.channelNumber)
                if (e.tvgId != null) obj.put("tvgId", e.tvgId)
                arr.put(obj)
            }
            savedPlaylistJsons[url] = arr.toString()
            prefs.edit().putString("playlist_entries_$url", arr.toString()).apply()
        }
    }

    private fun parseEntriesFromJson(json: String): List<PlaylistEntry> {
        val entries = mutableListOf<PlaylistEntry>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                entries.add(PlaylistEntry(
                    name = obj.optString("name", ""),
                    url = obj.optString("url", ""),
                    logo = obj.optString("logo", null),
                    groupTitle = obj.optString("groupTitle", null),
                    channelNumber = obj.optString("channelNumber", null),
                    tvgId = obj.optString("tvgId", null)
                ))
            }
        } catch (_: Exception) { }
        return entries
    }

    fun addStream(url: String) {
        val streamType = detectStreamType(url)

        val cached = cachedPlaylists[url]
        if (cached != null) {
            showCachedPlaylist(url, cached)
            return
        }

        if (streamType == StreamType.HLS || streamType == StreamType.HTTP) {
            loadPlaylist(url)
        } else {
            val stream = NetworkStream(url = url, type = streamType)
            val history = _uiState.value.streamHistory.toMutableList()
            history.add(0, stream)
            if (history.size > 50) history.removeAt(history.lastIndex)
            _uiState.value = _uiState.value.copy(streamHistory = history, urlInput = "")
        }
    }

    private fun showCachedPlaylist(url: String, entries: List<PlaylistEntry>) {
        val name = url.substringAfterLast("/").substringBefore("?").ifBlank { url }
        val existing = _uiState.value.savedPlaylists.toMutableList()
        if (existing.none { it.url == url }) {
            existing.add(0, SavedPlaylistData(name, url, entries.size))
            _uiState.value = _uiState.value.copy(savedPlaylists = existing)
            persistPlaylists()
        }
        _uiState.value = _uiState.value.copy(
            urlInput = "",
            isParsingPlaylist = false,
            isPlaylist = true,
            playlistEntries = entries,
            currentPlaylistUrl = url,
            playlistSearchQuery = ""
        )
    }

    private fun loadPlaylist(url: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isParsingPlaylist = true)
            try {
                val body = withContext(Dispatchers.IO) {
                    val request = Request.Builder().url(url).build()
                    httpClient.newCall(request).execute().body?.string() ?: ""
                }
                if (body.startsWith("#EXTM3U")) {
                    val entries = parseM3u(body)
                    cachedPlaylists[url] = entries
                    persistEntries(url, entries)
                    val name = url.substringAfterLast("/").substringBefore("?").ifBlank { url }
                    val existing = _uiState.value.savedPlaylists.toMutableList()
                    if (existing.none { it.url == url }) {
                        existing.add(0, SavedPlaylistData(name, url, entries.size))
                    }
                    _uiState.value = _uiState.value.copy(
                        isParsingPlaylist = false,
                        isPlaylist = true,
                        playlistEntries = entries,
                        currentPlaylistUrl = url,
                        playlistSearchQuery = "",
                        savedPlaylists = existing,
                        urlInput = ""
                    )
                    persistPlaylists()
                } else {
                    val stream = NetworkStream(url = url, type = detectStreamType(url))
                    val history = _uiState.value.streamHistory.toMutableList()
                    history.add(0, stream)
                    if (history.size > 50) history.removeAt(history.lastIndex)
                    _uiState.value = _uiState.value.copy(
                        isParsingPlaylist = false,
                        isPlaylist = false,
                        playlistEntries = emptyList(),
                        streamHistory = history,
                        urlInput = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isParsingPlaylist = false,
                    isPlaylist = false,
                    playlistEntries = emptyList(),
                    connectionError = "Failed to load: ${e.message}"
                )
            }
        }
    }

    fun reloadPlaylist(url: String) {
        cachedPlaylists.remove(url)
        loadPlaylist(url)
    }

    fun loadSavedPlaylist(url: String) {
        val cached = cachedPlaylists[url]
        if (cached != null) {
            showCachedPlaylist(url, cached)
        } else {
            loadPlaylist(url)
        }
    }

    fun removeSavedPlaylist(url: String) {
        val existing = _uiState.value.savedPlaylists.toMutableList()
        existing.removeAll { it.url == url }
        cachedPlaylists.remove(url)
        savedPlaylistJsons.remove(url)
        _uiState.value = _uiState.value.copy(savedPlaylists = existing)
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("playlists", Context.MODE_PRIVATE)
            prefs.edit().remove("playlist_entries_$url").apply()
            persistPlaylists()
        }
    }

    fun getSavedPlaylistsForHome(): List<SavedPlaylistData> = _uiState.value.savedPlaylists

    private fun parseM3u(content: String): List<PlaylistEntry> {
        val entries = mutableListOf<PlaylistEntry>()
        val lines = content.lines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF:")) {
                val info = line.removePrefix("#EXTINF:").trim()
                val name = info.substringAfterLast(",").trim()
                val params = info.substringBeforeLast(",").trim()
                val tvgId = Regex("tvg-id=\"([^\"]*)\"").find(params)?.groupValues?.getOrNull(1)
                val channelNumber = Regex("tvg-chno=\"([^\"]*)\"").find(params)?.groupValues?.getOrNull(1)
                val tvgName = Regex("tvg-name=\"([^\"]*)\"").find(params)?.groupValues?.getOrNull(1)
                val logo = Regex("tvg-logo=\"([^\"]*)\"").find(params)?.groupValues?.getOrNull(1)
                val groupTitle = Regex("group-title=\"([^\"]*)\"").find(params)?.groupValues?.getOrNull(1)
                val displayName = tvgName ?: name
                i++
                while (i < lines.size && lines[i].trim().isEmpty()) i++
                if (i < lines.size) {
                    val url = lines[i].trim()
                    if (!url.startsWith("#")) {
                        entries.add(PlaylistEntry(name = displayName, url = url, logo = logo, groupTitle = groupTitle, channelNumber = channelNumber, tvgId = tvgId))
                    }
                }
            }
            i++
        }
        return entries
    }

    fun onUrlInputChange(url: String) { _uiState.value = _uiState.value.copy(urlInput = url) }

    fun onPlaylistSearchQueryChanged(query: String) { _uiState.value = _uiState.value.copy(playlistSearchQuery = query) }

    fun clearPlaylist() {
        _uiState.value = _uiState.value.copy(isPlaylist = false, playlistEntries = emptyList(), currentPlaylistUrl = null, playlistSearchQuery = "")
    }

    fun filterByGroup(group: String?) { _uiState.value = _uiState.value.copy(selectedFilterGroup = group) }

    fun connectToSmb(host: String, share: String, username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConnecting = true)
            try { addStream("smb://$host/$share"); _uiState.value = _uiState.value.copy(isConnecting = false, connectionError = null) }
            catch (e: Exception) { _uiState.value = _uiState.value.copy(isConnecting = false, connectionError = e.message) }
        }
    }

    fun connectToFtp(host: String, port: Int, username: String, password: String, path: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConnecting = true)
            try { addStream("ftp://$username:$password@$host:$port$path"); _uiState.value = _uiState.value.copy(isConnecting = false, connectionError = null) }
            catch (e: Exception) { _uiState.value = _uiState.value.copy(isConnecting = false, connectionError = e.message) }
        }
    }

    fun connectToWebDav(host: String, path: String, username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConnecting = true)
            try { addStream("https://$host$path"); _uiState.value = _uiState.value.copy(isConnecting = false, connectionError = null) }
            catch (e: Exception) { _uiState.value = _uiState.value.copy(isConnecting = false, connectionError = e.message) }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(connectionError = null) }

    fun removeHistoryItem(index: Int) {
        val history = _uiState.value.streamHistory.toMutableList()
        if (index in history.indices) { history.removeAt(index); _uiState.value = _uiState.value.copy(streamHistory = history) }
    }

    fun clearHistory() { _uiState.value = _uiState.value.copy(streamHistory = emptyList()) }

    private fun detectStreamType(url: String): StreamType = when {
        url.contains(".m3u8") || url.contains(".m3u") -> StreamType.HLS
        url.contains(".mpd") -> StreamType.DASH
        url.startsWith("rtsp://") -> StreamType.RTSP
        url.startsWith("ftp://") -> StreamType.FTP
        url.startsWith("smb://") -> StreamType.SMB
        url.contains("webdav") -> StreamType.WEBDAV
        url.startsWith("udp://") -> StreamType.UDP
        url.startsWith("rtp://") -> StreamType.RTP
        else -> StreamType.HTTP
    }
}

data class SavedPlaylistData(val name: String, val url: String, val channelCount: Int)

data class NetworkUiState(
    val urlInput: String = "",
    val streamHistory: List<NetworkStream> = emptyList(),
    val isConnecting: Boolean = false,
    val connectionError: String? = null,
    val isParsingPlaylist: Boolean = false,
    val isPlaylist: Boolean = false,
    val playlistEntries: List<PlaylistEntry> = emptyList(),
    val currentPlaylistUrl: String? = null,
    val playlistSearchQuery: String = "",
    val selectedFilterGroup: String? = null,
    val savedPlaylists: List<SavedPlaylistData> = emptyList()
)
