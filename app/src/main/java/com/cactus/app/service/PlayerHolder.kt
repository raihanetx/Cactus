package com.cactus.app.service
import android.content.Context; import android.net.Uri
import androidx.media3.common.*; import androidx.media3.exoplayer.ExoPlayer
import com.cactus.app.data.audio.AudioExtractor; import com.cactus.app.data.model.Video; import com.cactus.app.data.repository.VideoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*; import kotlinx.coroutines.flow.*
import javax.inject.Inject; import javax.inject.Singleton
@Singleton
class PlayerHolder @Inject constructor(@ApplicationContext private val context: Context, private val audioExtractor: AudioExtractor, private val videoRepository: VideoRepository) {
    private var _state = MutableStateFlow(PlaybackUiState()); val state: StateFlow<PlaybackUiState> = _state.asStateFlow()
    @Volatile private var player: ExoPlayer? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate); private var pollJob: Job? = null
    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) { _state.value = _state.value.copy(isPlaying = isPlaying) }
        override fun onPlaybackStateChanged(playbackState: Int) { _state.value = _state.value.copy(isPrepared = playbackState == Player.STATE_READY, durationMs = if (playbackState == Player.STATE_READY) player?.duration?.takeIf { it > 0 } ?: 0L else 0L) }
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) { _state.value = _state.value.copy(title = mediaItem?.mediaMetadata?.title?.toString() ?: "Cactus") }
    }
    private fun ensurePlayer(): ExoPlayer = player ?: synchronized(this) { player ?: ExoPlayer.Builder(context).setAudioAttributes(AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).setUsage(C.USAGE_MEDIA).build(), true).setHandleAudioBecomingNoisy(true).build().also { p -> p.addListener(listener); player = p; startPolling(p) } }
    private fun startPolling(p: ExoPlayer) { pollJob?.cancel(); pollJob = scope.launch { while (true) { delay(500); _state.value = _state.value.copy(currentPositionMs = p.currentPosition.coerceAtLeast(0L), durationMs = p.duration.takeIf { it > 0 } ?: 0L) } } }
    fun playVideo(video: Video, uri: Uri) {
        val p = ensurePlayer()
        p.setMediaItem(MediaItem.Builder().setUri(uri).setMediaId(video.id.toString()).setMediaMetadata(MediaMetadata.Builder().setTitle(video.displayName).setArtist("Cactus").build()).build()); p.prepare(); p.playWhenReady = true
        _state.value = _state.value.copy(title = video.displayName, videoId = video.id, uri = uri)
        if (video.audioExtractedPath == null && video.filePath.isNotEmpty()) { scope.launch { try { val path = audioExtractor.extract(video.filePath); videoRepository.setAudioExtractedPath(video.id, path) } catch (_: Exception) {} } }
    }
    fun togglePlayPause() { val p = player ?: return; if (p.isPlaying) p.pause() else p.play() }
    fun seekTo(positionMs: Long) { player?.seekTo(positionMs.coerceAtLeast(0L)); _state.value = _state.value.copy(currentPositionMs = positionMs) }
    fun seekRelative(deltaMs: Long) { val p = player ?: return; val target = (p.currentPosition + deltaMs).coerceAtLeast(0L).coerceAtMost(p.duration.takeIf { it > 0 } ?: Long.MAX_VALUE); p.seekTo(target); _state.value = _state.value.copy(currentPositionMs = target) }
    fun release() { pollJob?.cancel(); player?.removeListener(listener); player?.release(); player = null; _state.value = PlaybackUiState() }
    fun exoPlayerForSession(): ExoPlayer = ensurePlayer()
    fun handleAction(action: String) { when (action) { PlaybackService.ACTION_TOGGLE -> togglePlayPause(); PlaybackService.ACTION_SEEK_BACK -> seekRelative(-10_000L); PlaybackService.ACTION_SEEK_FORWARD -> seekRelative(10_000L); PlaybackService.ACTION_STOP -> { player?.pause(); _state.value = _state.value.copy(isPlaying = false) } } }
}
data class PlaybackUiState(val isPlaying: Boolean = false, val isPrepared: Boolean = false, val currentPositionMs: Long = 0L, val durationMs: Long = 0L, val title: String = "", val videoId: Long? = null, val uri: Uri? = null)
