package com.cactus.app.ui.navigation
sealed class Route(val path: String) {
    data object Home : Route("home")
    data object Player : Route("player/{videoId}") { fun build(id: Long) = "player/$id" }
    data object Settings : Route("settings")
}
