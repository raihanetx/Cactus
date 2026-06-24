package com.cactus.app.data.repository
import com.cactus.app.data.db.dao.SubtitleCueDao; import com.cactus.app.data.db.mapper.toDomain; import com.cactus.app.data.db.mapper.toEntity; import com.cactus.app.data.model.SubtitleCue; import com.cactus.app.data.model.SubtitleLanguage
import kotlinx.coroutines.flow.Flow; import kotlinx.coroutines.flow.map
import javax.inject.Inject; import javax.inject.Singleton
interface SubtitleRepository { fun observeForVideo(videoId: Long, language: SubtitleLanguage): Flow<List<SubtitleCue>>; suspend fun saveCues(cues: List<SubtitleCue>) }
@Singleton
class SubtitleRepositoryImpl @Inject constructor(private val dao: SubtitleCueDao) : SubtitleRepository {
    override fun observeForVideo(videoId: Long, language: SubtitleLanguage): Flow<List<SubtitleCue>> = dao.observeForVideo(videoId, language.name).map { it.map { e -> e.toDomain() } }
    override suspend fun saveCues(cues: List<SubtitleCue>) = dao.insertAll(cues.map { it.toEntity() })
}
