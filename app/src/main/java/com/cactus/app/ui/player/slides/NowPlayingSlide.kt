package com.cactus.app.ui.player.slides
import android.content.Intent; import android.net.Uri
import androidx.compose.foundation.background; import androidx.compose.foundation.layout.*; import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons; import androidx.compose.material.icons.filled.MoreHoriz; import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*; import androidx.compose.runtime.*; import androidx.compose.ui.Alignment; import androidx.compose.ui.Modifier; import androidx.compose.ui.draw.clip; import androidx.compose.ui.graphics.Brush; import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight; import androidx.compose.ui.text.style.TextAlign; import androidx.compose.ui.text.style.TextOverflow; import androidx.compose.ui.unit.dp; import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat; import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cactus.app.data.model.Video; import com.cactus.app.service.PlaybackService; import com.cactus.app.ui.components.PaginationDots; import com.cactus.app.ui.components.PlayerDock; import com.cactus.app.ui.components.PlayerHeader
import com.cactus.app.ui.player.LocalPlayerHolder; import com.cactus.app.ui.theme.CactusColors; import com.cactus.app.util.TimeUtils
@Composable
fun NowPlayingSlide(videoId: Long?, videoTitle: String, videoUri: Uri?, onDismiss: () -> Unit) {
    val context = LocalContext.current; val holder = LocalPlayerHolder.current; val state by holder.state.collectAsStateWithLifecycle()
    LaunchedEffect(videoUri, videoId) { if (videoUri != null && videoId != null && state.videoId != videoId) { val video = Video(id = videoId, mediaStoreId = videoId, displayName = videoTitle, filePath = "", sizeBytes = 0L, durationMs = 0L, dateAddedMs = 0L, importedAt = 0L); holder.playVideo(video, videoUri); ContextCompat.startForegroundService(context, Intent(context, PlaybackService::class.java)) } }
    val displayTitle = state.title.ifBlank { videoTitle }; val progressFraction = if (state.durationMs > 0) (state.currentPositionMs.toFloat() / state.durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    Column(modifier = Modifier.fillMaxSize().background(CactusColors.White)) {
        PlayerHeader(title = "Now Playing", subtitle = displayTitle.ifBlank { "Cactus" }, onDismiss = onDismiss, rightAction = { Icon(Icons.Filled.MoreHoriz, contentDescription = null, tint = CactusColors.Neutral500, modifier = Modifier.size(20.dp)) })
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 32.dp)) {
            Box(modifier = Modifier.size(180.dp).clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(CactusColors.Neutral800, CactusColors.Black))), contentAlignment = Alignment.Center) { Icon(Icons.Filled.MusicNote, contentDescription = null, tint = CactusColors.White, modifier = Modifier.size(64.dp)) }
            Spacer(Modifier.height(40.dp))
            Text(displayTitle.ifBlank { "Cactus" }, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = CactusColors.Black, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis, letterSpacing = (-0.3).sp)
            Spacer(Modifier.height(12.dp))
            Text(if (state.isPlaying) "Now playing" else "Tap play to start listening", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = CactusColors.Neutral500)
            Spacer(Modifier.height(16.dp))
            Text("${TimeUtils.formatDurationShort(state.currentPositionMs)}  /  ${TimeUtils.formatDurationShort(state.durationMs)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CactusColors.Neutral400, letterSpacing = 0.5.sp)
        }
        PlayerDock(progressFraction = progressFraction, currentTimeLabel = TimeUtils.formatDuration(state.currentPositionMs), totalTimeLabel = TimeUtils.formatDuration(state.durationMs), centerButtonLabel = if (state.isPlaying) "Pause" else "Play", isPlaying = state.isPlaying, onCenterClick = { holder.togglePlayPause() }, onSeekBack = { holder.seekRelative(-15_000L) }, onSeekForward = { holder.seekRelative(15_000L) })
        PaginationDots(activeIndex = 0)
    }
}
