package com.cactus.app.ui.player.slides
import android.content.Context
import androidx.lifecycle.ViewModel; import androidx.lifecycle.viewModelScope
import androidx.work.*; import com.cactus.app.data.model.*; import com.cactus.app.data.repository.SubtitleRepository; import com.cactus.app.data.subtitle.SubtitlePipeline; import com.cactus.app.data.subtitle.SubtitlePipelineWorker
import dagger.hilt.android.lifecycle.HiltViewModel; import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*; import kotlinx.coroutines.launch
import javax.inject.Inject
data class DialogueUiState(val isTranslating: Boolean = false, val errorMessage: String? = null)
@HiltViewModel
class DialogueViewModel @Inject constructor(@ApplicationContext private val context: Context, private val subtitleRepository: SubtitleRepository, private val pipeline: SubtitlePipeline) : ViewModel() {
    private val _uiState = MutableStateFlow(DialogueUiState()); val uiState: StateFlow<DialogueUiState> = _uiState.asStateFlow()
    private val _activeVideoId = MutableStateFlow<Long?>(null)
    val englishCues: StateFlow<List<SubtitleCue>> = _activeVideoId.flatMapLatest { id -> if (id == null) flowOf(emptyList()) else subtitleRepository.observeForVideo(id, SubtitleLanguage.EN) }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val banglaCues: StateFlow<List<SubtitleCue>> = _activeVideoId.flatMapLatest { id -> if (id == null) flowOf(emptyList()) else subtitleRepository.observeForVideo(id, SubtitleLanguage.BN) }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val pipelineProgress = pipeline.progress.let { flow -> kotlinx.coroutines.flow.combine(flow, _activeVideoId) { all, id -> if (id == null) null else all[id] } }.stateIn(viewModelScope, SharingStarted.Lazily, null)
    fun setActiveVideo(videoId: Long) { _activeVideoId.value = videoId }
    fun startTranslation(video: Video) { if (_uiState.value.isTranslating) return; _uiState.value = _uiState.value.copy(isTranslating = true, errorMessage = null); val workName = SubtitlePipelineWorker.UNIQUE_WORK_NAME_PREFIX + video.id; WorkManager.getInstance(context).enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<SubtitlePipelineWorker>().setInputData(workDataOf(SubtitlePipelineWorker.KEY_VIDEO_ID to video.id)).build()); viewModelScope.launch { WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(workName).collect { list -> val info = list.firstOrNull() ?: return@collect; when (info.state) { WorkInfo.State.SUCCEEDED -> _uiState.value = _uiState.value.copy(isTranslating = false, errorMessage = null); WorkInfo.State.FAILED -> _uiState.value = _uiState.value.copy(isTranslating = false, errorMessage = info.outputData.getString(SubtitlePipelineWorker.KEY_ERROR) ?: "Translation failed"); WorkInfo.State.CANCELLED -> _uiState.value = _uiState.value.copy(isTranslating = false); WorkInfo.State.RUNNING -> _uiState.value = _uiState.value.copy(isTranslating = true); else -> {} } } } }
}
