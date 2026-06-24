package com.cactus.app.ui.player.slides
import androidx.lifecycle.ViewModel; import androidx.lifecycle.viewModelScope; import com.cactus.app.data.model.Note; import com.cactus.app.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel; import kotlinx.coroutines.flow.*; import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class NotesViewModel @Inject constructor(private val noteRepository: NoteRepository) : ViewModel() {
    private val _activeVideoId = MutableStateFlow<Long?>(null)
    val notes: StateFlow<List<Note>> = _activeVideoId.flatMapLatest { id -> if (id == null) flowOf(emptyList()) else noteRepository.observeForVideo(id) }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    fun setActiveVideo(videoId: Long) { _activeVideoId.value = videoId }
    fun createNote(videoId: Long, text: String, positionMs: Long) { if (text.isBlank()) return; viewModelScope.launch { val now = System.currentTimeMillis(); val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }.size; noteRepository.insert(Note(0L, videoId, positionMs, text.trim(), words, now, now)) } }
    fun deleteNote(id: Long) { viewModelScope.launch { noteRepository.delete(id) } }
}
