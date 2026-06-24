package com.cactus.app.data.repository
import com.cactus.app.data.db.dao.NoteDao; import com.cactus.app.data.db.mapper.toDomain; import com.cactus.app.data.db.mapper.toEntity; import com.cactus.app.data.model.Note
import kotlinx.coroutines.flow.Flow; import kotlinx.coroutines.flow.map
import javax.inject.Inject; import javax.inject.Singleton
interface NoteRepository { fun observeForVideo(videoId: Long): Flow<List<Note>>; suspend fun insert(note: Note): Long; suspend fun delete(id: Long) }
@Singleton
class NoteRepositoryImpl @Inject constructor(private val dao: NoteDao) : NoteRepository {
    override fun observeForVideo(videoId: Long): Flow<List<Note>> = dao.observeForVideo(videoId).map { it.map { e -> e.toDomain() } }
    override suspend fun insert(note: Note): Long = dao.insert(note.toEntity())
    override suspend fun delete(id: Long) = dao.deleteById(id)
}
