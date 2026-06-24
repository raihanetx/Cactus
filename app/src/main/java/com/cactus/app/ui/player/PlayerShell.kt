package com.cactus.app.ui.player
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize; import androidx.compose.foundation.pager.HorizontalPager; import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable; import androidx.compose.runtime.remember; import androidx.compose.ui.Modifier
import com.cactus.app.VideoUriHolder; import com.cactus.app.data.model.Video; import com.cactus.app.ui.player.slides.*
@Composable
fun PlayerShell(videoId: Long?, videoTitle: String, videoUri: Uri?, onDismiss: () -> Unit) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
    val video = remember(videoId) { videoId?.let { id -> Video(id = id, mediaStoreId = id, displayName = videoTitle, filePath = VideoUriHolder.pathByVideoId[id] ?: "", sizeBytes = 0L, durationMs = 0L, dateAddedMs = 0L, importedAt = 0L) } }
    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
        when (page) { 0 -> NowPlayingSlide(videoId = videoId, videoTitle = videoTitle, videoUri = videoUri, onDismiss = onDismiss); 1 -> DialogueSlide(video = video, onDismiss = onDismiss); 2 -> LoopsSlide(videoId = videoId, onDismiss = onDismiss); 3 -> NotesSlide(videoId = videoId, onDismiss = onDismiss) }
    }
}
