package com.cactus.app.ui.player
import androidx.compose.runtime.staticCompositionLocalOf; import com.cactus.app.service.PlayerHolder
val LocalPlayerHolder = staticCompositionLocalOf<PlayerHolder> { error("PlayerHolder not provided") }
