package com.cactus.app.data.repository
import com.cactus.app.data.db.dao.VideoDao; import com.cactus.app.data.db.entity.VideoEntity; import com.cactus.app.data.db.mapper.toDomain; import com.cactus.app.data.media.MediaStoreVideo; import com.cactus.app.data.model.SubtitleStatus; import com.cactus.app.data.model.Video
import kotlinx.coroutines.flow.Flow; import kotlinx.coroutines.flow.map
import javax.inject.Inject; import javax.inject.Singleton
enum class VideoFilter { NEW, LARGE_TO_SMALL, SMALL_TO_LARGE, WITH_SUBTITLES, WITHOUT_SUBTITLES }
interface VideoRepository { fun observeForFilter(filter: VideoFilter): Flow<List<Video>>; suspend fun getById(id: Long): Video?; suspend fun importFromMediaStore(scanned: List<MediaStoreVideo>); suspend fun setAudioExtractedPath(videoId: Long, path: String); suspend fun setSubtitleStatus(videoId: Long, status: SubtitleStatus, pct: Int); suspend fun setLastPlayedAt(videoId: Long, timestamp: Long) }
@Singleton
class VideoRepositoryImpl @Inject constructor(private val videoDao: VideoDao) : VideoRepository {
    override fun observeForFilter(filter: VideoFilter): Flow<List<Video>> = when (filter) { VideoFilter.NEW -> videoDao.observeAllByNewest(); VideoFilter.LARGE_TO_SMALL -> videoDao.observeAllBySizeDesc(); VideoFilter.SMALL_TO_LARGE -> videoDao.observeAllBySizeAsc(); VideoFilter.WITH_SUBTITLES -> videoDao.observeWithSubtitles(); VideoFilter.WITHOUT_SUBTITLES -> videoDao.observeWithoutSubtitles() }.map { it.map(VideoEntity::toDomain) }
    override suspend fun getById(id: Long): Video? = videoDao.getById(id)?.toDomain()
    override suspend fun importFromMediaStore(scanned: List<MediaStoreVideo>) { val now = System.currentTimeMillis(); for (item in scanned) { val existing = videoDao.getByMediaStoreId(item.mediaStoreId); if (existing == null) { videoDao.insert(VideoEntity(mediaStoreId = item.mediaStoreId, displayName = item.displayName, filePath = item.filePath, sizeBytes = item.sizeBytes, durationMs = item.durationMs, dateAddedMs = item.dateAddedMs, subtitleStatus = SubtitleStatus.NOT_STARTED.name, importedAt = now)) } else { videoDao.update(existing.copy(displayName = item.displayName, filePath = item.filePath, sizeBytes = item.sizeBytes, durationMs = item.durationMs, dateAddedMs = item.dateAddedMs)) } } }
    override suspend fun setAudioExtractedPath(videoId: Long, path: String) = videoDao.setAudioExtractedPath(videoId, path)
    override suspend fun setSubtitleStatus(videoId: Long, status: SubtitleStatus, pct: Int) = videoDao.setSubtitleStatus(videoId, status.name, pct)
    override suspend fun setLastPlayedAt(videoId: Long, timestamp: Long) = videoDao.setLastPlayedAt(videoId, timestamp)
}
