package com.cactus.app.data.repository
import com.cactus.app.data.db.dao.LoopDao; import com.cactus.app.data.db.mapper.toDomain; import com.cactus.app.data.db.mapper.toEntity; import com.cactus.app.data.model.Loop
import kotlinx.coroutines.flow.Flow; import kotlinx.coroutines.flow.map
import javax.inject.Inject; import javax.inject.Singleton
interface LoopRepository { fun observeForVideo(videoId: Long): Flow<List<Loop>>; suspend fun insert(loop: Loop): Long; suspend fun delete(id: Long) }
@Singleton
class LoopRepositoryImpl @Inject constructor(private val dao: LoopDao) : LoopRepository {
    override fun observeForVideo(videoId: Long): Flow<List<Loop>> = dao.observeForVideo(videoId).map { it.map { e -> e.toDomain() } }
    override suspend fun insert(loop: Loop): Long = dao.insert(loop.toEntity())
    override suspend fun delete(id: Long) = dao.deleteById(id)
}
