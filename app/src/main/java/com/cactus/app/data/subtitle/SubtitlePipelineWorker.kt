package com.cactus.app.data.subtitle
import android.content.Context; import android.util.Log
import androidx.hilt.work.HiltWorker; import androidx.work.CoroutineWorker; import androidx.work.Data; import androidx.work.WorkerParameters
import com.cactus.app.data.repository.VideoRepository
import dagger.assisted.Assisted; import dagger.assisted.AssistedInject
@HiltWorker
class SubtitlePipelineWorker @AssistedInject constructor(@Assisted appContext: Context, @Assisted params: WorkerParameters, private val videoRepository: VideoRepository, private val pipeline: SubtitlePipeline) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val videoId = inputData.getLong(KEY_VIDEO_ID, -1L); if (videoId < 0) return Result.failure()
        val video = videoRepository.getById(videoId) ?: return Result.failure()
        val result = pipeline.generate(video) { isStopped }
        return if (result.isSuccess) Result.success(Data.Builder().putLong(KEY_VIDEO_ID, videoId).build()) else Result.failure(Data.Builder().putString(KEY_ERROR, result.exceptionOrNull()?.message).build())
    }
    companion object { const val TAG = "SubtitleWorker"; const val KEY_VIDEO_ID = "video_id"; const val KEY_ERROR = "error_message"; const val UNIQUE_WORK_NAME_PREFIX = "subtitle_pipeline_" }
}
