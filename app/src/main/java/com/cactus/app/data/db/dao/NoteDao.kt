package com.cactus.app.data.db.dao
import androidx.room.*; import com.cactus.app.data.db.entity.NoteEntity; import kotlinx.coroutines.flow.Flow
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE videoId = :videoId ORDER BY positionMs ASC") fun observeForVideo(videoId: Long): Flow<List<NoteEntity>>
    @Query("SELECT * FROM notes WHERE videoId = :videoId ORDER BY positionMs ASC") suspend fun getForVideo(videoId: Long): List<NoteEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(note: NoteEntity): Long
    @Update suspend fun update(note: NoteEntity)
    @Query("DELETE FROM notes WHERE id = :id") suspend fun deleteById(id: Long)
}
