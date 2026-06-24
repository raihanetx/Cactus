package com.cactus.app.ui.home
import android.app.Application; import android.content.pm.PackageManager; import android.os.Build
import androidx.core.content.ContextCompat; import androidx.lifecycle.AndroidViewModel; import androidx.lifecycle.viewModelScope
import com.cactus.app.data.media.MediaStoreScanner; import com.cactus.app.data.model.Video; import com.cactus.app.data.repository.VideoFilter; import com.cactus.app.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*; import kotlinx.coroutines.launch
import javax.inject.Inject
enum class PermissionState { UNKNOWN, GRANTED, DENIED, PERMANENTLY_DENIED }
data class HomeUiState(val permissionState: PermissionState = PermissionState.UNKNOWN, val isScanning: Boolean = false, val selectedFilter: VideoFilter = VideoFilter.NEW, val searchQuery: String = "")
@HiltViewModel
class HomeViewModel @Inject constructor(private val app: Application, private val videoRepository: VideoRepository) : AndroidViewModel(app) {
    private val _uiState = MutableStateFlow(HomeUiState()); val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    val videos: StateFlow<List<Video>> = _uiState.flatMapLatest { state -> videoRepository.observeForFilter(state.selectedFilter) }.combine(_uiState) { list, state -> if (state.searchQuery.isBlank()) list else list.filter { it.displayName.contains(state.searchQuery, ignoreCase = true) } }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    init { _uiState.value = _uiState.value.copy(permissionState = if (hasMediaPermission()) PermissionState.GRANTED else PermissionState.UNKNOWN) }
    fun onPermissionResult(granted: Boolean, canStillAsk: Boolean) { _uiState.value = _uiState.value.copy(permissionState = when { granted -> PermissionState.GRANTED; canStillAsk -> PermissionState.DENIED; else -> PermissionState.PERMANENTLY_DENIED }); if (granted) scanMediaStore() }
    fun setSelectedFilter(filter: VideoFilter) { _uiState.value = _uiState.value.copy(selectedFilter = filter) }
    fun setSearchQuery(query: String) { _uiState.value = _uiState.value.copy(searchQuery = query) }
    fun scanMediaStore() { if (_uiState.value.isScanning) return; viewModelScope.launch { _uiState.value = _uiState.value.copy(isScanning = true); val scanned = MediaStoreScanner.scanVideos(app); videoRepository.importFromMediaStore(scanned); _uiState.value = _uiState.value.copy(isScanning = false) } }
    fun requiredPermissions(): Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(android.Manifest.permission.READ_MEDIA_VIDEO, android.Manifest.permission.READ_MEDIA_AUDIO, android.Manifest.permission.POST_NOTIFICATIONS) else arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    private fun hasMediaPermission(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ContextCompat.checkSelfPermission(app, android.Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(app, android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED else ContextCompat.checkSelfPermission(app, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
}
