package com.example.ui.viewmodel

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.VideoEntity
import com.example.data.model.VideoMetadata
import com.example.data.model.VideoQualityStream
import com.example.data.repository.VideoRepository
import com.example.data.service.DownloadState
import com.example.ui.util.AppLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(private val repository: VideoRepository) : ViewModel() {

    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _parsedMetadata = MutableStateFlow<VideoMetadata?>(null)
    val parsedMetadata: StateFlow<VideoMetadata?> = _parsedMetadata.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val _isQualityDialogVisible = MutableStateFlow(false)
    val isQualityDialogVisible: StateFlow<Boolean> = _isQualityDialogVisible.asStateFlow()

    private val _isSettingsVisible = MutableStateFlow(false)
    val isSettingsVisible: StateFlow<Boolean> = _isSettingsVisible.asStateFlow()

    private val _videoToDelete = MutableStateFlow<VideoEntity?>(null)
    val videoToDelete: StateFlow<VideoEntity?> = _videoToDelete.asStateFlow()

    private val _playingVideo = MutableStateFlow<VideoEntity?>(null)
    val playingVideo: StateFlow<VideoEntity?> = _playingVideo.asStateFlow()

    private val _appLanguage = MutableStateFlow(AppLanguage.ARABIC)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _autoClearClipboard = MutableStateFlow(true)
    val autoClearClipboard: StateFlow<Boolean> = _autoClearClipboard.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var downloadJob: Job? = null

    val recentVideos: StateFlow<List<VideoEntity>> = repository.recentVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVideos: StateFlow<List<VideoEntity>> = repository.allVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onUrlChanged(newUrl: String) {
        _urlInput.value = newUrl
        if (newUrl.isBlank()) {
            _parsedMetadata.value = null
            _errorMessage.value = null
        }
    }

    fun pasteFromClipboard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
            val item = clipboard.primaryClip?.getItemAt(0)
            val text = item?.text?.toString() ?: ""
            if (text.isNotBlank()) {
                _urlInput.value = text
                analyzeLink()
                if (_autoClearClipboard.value) {
                    try {
                        clipboard.clearPrimaryClip()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun clearInput() {
        _urlInput.value = ""
        _parsedMetadata.value = null
        _errorMessage.value = null
    }

    fun analyzeLink() {
        val url = _urlInput.value.trim()
        if (url.isBlank()) {
            _errorMessage.value = "invalid_url_error"
            return
        }

        viewModelScope.launch {
            _isAnalyzing.value = true
            _errorMessage.value = null

            val result = repository.parseLink(url)
            _isAnalyzing.value = false

            result.onSuccess { metadata ->
                _parsedMetadata.value = metadata
                _isQualityDialogVisible.value = true
            }.onFailure {
                _errorMessage.value = "invalid_url_error"
            }
        }
    }

    fun dismissQualityDialog() {
        _isQualityDialogVisible.value = false
    }

    fun startDownloadWithQuality(stream: VideoQualityStream) {
        val metadata = _parsedMetadata.value ?: return
        _isQualityDialogVisible.value = false

        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            repository.downloadVideo(stream.downloadUrl, metadata.platform, metadata.title)
                .collect { state ->
                    _downloadState.value = state
                    if (state is DownloadState.Completed) {
                        repository.saveVideoRecord(
                            title = metadata.title,
                            platform = metadata.platform.displayName,
                            originalUrl = metadata.originalUrl,
                            thumbnailUrl = metadata.thumbnailUrl,
                            localFilePath = state.localFilePath,
                            fileSizeBytes = state.fileSizeBytes,
                            durationText = metadata.durationText,
                            qualityLabel = stream.quality.resolutionLabel
                        )
                    }
                }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        _downloadState.value = DownloadState.Idle
    }

    fun resetDownloadState() {
        _downloadState.value = DownloadState.Idle
    }

    fun showDeleteConfirmation(video: VideoEntity) {
        _videoToDelete.value = video
    }

    fun dismissDeleteConfirmation() {
        _videoToDelete.value = null
    }

    fun confirmDelete() {
        val video = _videoToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteVideo(video)
            _videoToDelete.value = null
        }
    }

    fun playVideo(video: VideoEntity) {
        _playingVideo.value = video
    }

    fun dismissPlayer() {
        _playingVideo.value = null
    }

    fun shareVideo(context: Context, video: VideoEntity) {
        try {
            val file = File(video.localFilePath)
            if (!file.exists()) return

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "video/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openSettings() {
        _isSettingsVisible.value = true
    }

    fun dismissSettings() {
        _isSettingsVisible.value = false
    }

    fun toggleLanguage() {
        _appLanguage.value = if (_appLanguage.value == AppLanguage.ARABIC) AppLanguage.ENGLISH else AppLanguage.ARABIC
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun toggleAutoClearClipboard() {
        _autoClearClipboard.value = !_autoClearClipboard.value
    }

    fun dismissError() {
        _errorMessage.value = null
    }
}

class MainViewModelFactory(private val repository: VideoRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
