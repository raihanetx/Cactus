package com.cactus.app.data.subtitle
import android.util.Log
import com.cactus.app.data.AppConfig; import com.cactus.app.data.audio.AudioExtractor; import com.cactus.app.data.model.*; import com.cactus.app.data.network.*; import com.cactus.app.data.repository.SubtitleRepository; import com.cactus.app.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow; import kotlinx.coroutines.flow.StateFlow; import kotlinx.coroutines.flow.asStateFlow; import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import javax.inject.Inject; import javax.inject.Singleton
@Singleton
class SubtitlePipeline @Inject constructor(private val audioExtractor: AudioExtractor, private val groqApi: GroqApiService, private val subtitleRepository: SubtitleRepository, private val videoRepository: VideoRepository) {
    private val _progress = MutableStateFlow<Map<Long, PipelineProgress>>(emptyMap())
    val progress: StateFlow<Map<Long, PipelineProgress>> = _progress.asStateFlow()
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    suspend fun generate(video: Video, isCancelled: () -> Boolean = { false }): Result<Unit> {
        return try {
            updateProgress(video.id, SubtitleStatus.EXTRACTING, 5, "Extracting audio...")
            val audioPath = video.audioExtractedPath ?: audioExtractor.extract(video.filePath)
            videoRepository.setAudioExtractedPath(video.id, audioPath)
            if (isCancelled()) { videoRepository.setSubtitleStatus(video.id, SubtitleStatus.PARTIAL, 0); return Result.success(Unit) }
            updateProgress(video.id, SubtitleStatus.TRANSCRIBING, 15, "Transcribing with Whisper...")
            val whisperResp = groqApi.transcribeAudio(java.io.File(audioPath))
            val englishCues = whisperResp.segments.mapIndexed { idx, seg -> SubtitleCue(0L, video.id, SubtitleLanguage.EN, idx, (seg.start * 1000).toLong(), (seg.end * 1000).toLong(), seg.text.trim(), null) }
            subtitleRepository.saveCues(englishCues)
            if (isCancelled()) { videoRepository.setSubtitleStatus(video.id, SubtitleStatus.PARTIAL, 50); return Result.success(Unit) }
            updateProgress(video.id, SubtitleStatus.TRANSLATING, 50, "Translating to Bangla...")
            val batches = englishCues.chunked(AppConfig.TRANSLATION_BATCH_SIZE); val allBnCues = mutableListOf<SubtitleCue>()
            batches.forEachIndexed { batchIdx, batch ->
                if (isCancelled()) { if (allBnCues.isNotEmpty()) subtitleRepository.saveCues(allBnCues); videoRepository.setSubtitleStatus(video.id, SubtitleStatus.PARTIAL, 50 + (batchIdx * 45 / batches.size)); return Result.success(Unit) }
                updateProgress(video.id, SubtitleStatus.TRANSLATING, 50 + ((batchIdx + 1) * 45 / batches.size), "Batch ${batchIdx + 1}/${batches.size}...")
                val items = translateBatch(batch); items.forEach { item -> val src = batch.getOrNull(item.id) ?: return@forEach; allBnCues.add(SubtitleCue(0L, video.id, SubtitleLanguage.BN, src.cueIndex, src.startMs, src.endMs, item.bnText, item.pronunciation)) }
            }
            subtitleRepository.saveCues(allBnCues); videoRepository.setSubtitleStatus(video.id, SubtitleStatus.READY, 100); updateProgress(video.id, SubtitleStatus.READY, 100, "Done"); Result.success(Unit)
        } catch (e: Exception) { Log.e("SubtitlePipeline", "Failed", e); videoRepository.setSubtitleStatus(video.id, SubtitleStatus.FAILED, 0); updateProgress(video.id, SubtitleStatus.FAILED, 0, e.message ?: "Unknown error"); Result.failure(e) }
    }
    private suspend fun translateBatch(cues: List<SubtitleCue>): List<TranslationItem> {
        val prompt = buildString { append("Translate each English subtitle cue to Bangla (bn_text) and provide American English pronunciation in Bangla script with hyphens between syllables.\nReturn JSON array: [{\"id\": int, \"bn_text\": \"bangla\", \"pronunciation\": \"bangla hyphenated\"}]\nOutput ONLY the JSON array.\n\nCues:\n"); cues.forEach { append("{\"id\": ${it.cueIndex}, \"text\": \"${it.text.replace("\"", "\\\"")}\"}\n") } }
        val resp = groqApi.translate(listOf(ChatMessage("system", "You are a precise English-to-Bangla translator. Always respond with valid JSON."), ChatMessage("user", prompt)))
        val content = resp.choices.firstOrNull()?.message?.content ?: throw IllegalStateException("No content")
        val start = content.indexOf('['); val end = content.lastIndexOf(']')
        val jsonStr = if (start >= 0 && end > start) content.substring(start, end + 1) else throw IllegalStateException("No JSON array")
        return json.decodeFromString<List<TranslationItem>>(jsonStr)
    }
    private fun updateProgress(videoId: Long, status: SubtitleStatus, pct: Int, message: String) { _progress.update { it + (videoId to PipelineProgress(status, pct, message)) } }
    data class PipelineProgress(val status: SubtitleStatus, val pct: Int, val message: String)
}
