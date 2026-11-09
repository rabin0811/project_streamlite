package com.streamlite.app.ui.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val PLAYER = "player/{mediaId}"
    const val IMAGE_VIEWER = "imageViewer/{mediaId}"
    const val PLAYLIST = "playlist/{playlistId}"
    const val FOLDER = "folder/{folderPath}"
    const val NETWORK = "network"
    const val SEARCH = "search"
    const val FAVORITES = "favorites"
    const val RECENTLY_PLAYED = "recentlyPlayed"

    fun playerRoute(mediaId: String) = "player/${java.net.URLEncoder.encode(mediaId, "UTF-8")}"
    fun imageViewerRoute(mediaId: String) = "imageViewer/${java.net.URLEncoder.encode(mediaId, "UTF-8")}"
    fun playlistRoute(playlistId: Long) = "playlist/$playlistId"
    fun folderRoute(folderPath: String) = "folder/${java.net.URLEncoder.encode(folderPath, "UTF-8")}"
}
