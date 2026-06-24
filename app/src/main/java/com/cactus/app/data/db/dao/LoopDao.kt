package com.cactus.app.data.db.dao
import androidx.room.*; import com.cactus.app.data.db.entity.LoopEntity; import kotlinx.coroutines.flow.Flow
@Dao
interface LoopDao {
    @Query("SELECT * FROM loops WHERE videoId = :videoId ORDER BY createdAt DESC") fun observeForVideo(videoId: Long): Flow<List<LoopEntity>>
    @Query("SELECT * FROM loops WHERE videoId = :videoId ORDER BY createdAt DESC") suspend fun getForVideo(videoId: Long): List<LoopEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(loop: LoopEntity): Long
    @Update suspend fun update(loop: LoopEntity)
    @Query("DELETE FROM loops WHERE id = :id") suspend fun deleteById(id: Long)
}
