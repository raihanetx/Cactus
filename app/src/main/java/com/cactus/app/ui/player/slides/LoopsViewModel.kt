package com.cactus.app.ui.player.slides
import androidx.lifecycle.ViewModel; import androidx.lifecycle.viewModelScope; import com.cactus.app.data.model.Loop; import com.cactus.app.data.repository.LoopRepository; import com.cactus.app.service.PlayerHolder
import dagger.hilt.android.lifecycle.HiltViewModel; import kotlinx.coroutines.flow.*; import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class LoopsViewModel @Inject constructor(private val loopRepository: LoopRepository) : ViewModel() {
    private val _activeVideoId = MutableStateFlow<Long?>(null)
    val loops: StateFlow<List<Loop>> = _activeVideoId.flatMapLatest { id -> if (id == null) flowOf(emptyList()) else loopRepository.observeForVideo(id) }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    fun setActiveVideo(videoId: Long) { _activeVideoId.value = videoId }
    fun createLoop(videoId: Long, name: String, startMs: Long, endMs: Long, loopCount: Int) { viewModelScope.launch { val now = System.currentTimeMillis(); loopRepository.insert(Loop(0L, videoId, name.ifBlank { "Untitled Loop" }, startMs, endMs, loopCount, now, now)) } }
    fun deleteLoop(id: Long) { viewModelScope.launch { loopRepository.delete(id) } }
    fun playLoop(loop: Loop, holder: PlayerHolder) { holder.seekTo(loop.startMs); holder.togglePlayPause() }
}
