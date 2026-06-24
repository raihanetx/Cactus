package com.cactus.app.data.db.dao
import androidx.room.*; import com.cactus.app.data.db.entity.SubtitleCueEntity; import kotlinx.coroutines.flow.Flow
@Dao
interface SubtitleCueDao {
    @Query("SELECT * FROM subtitle_cues WHERE videoId = :videoId AND language = :language ORDER BY cueIndex ASC") fun observeForVideo(videoId: Long, language: String): Flow<List<SubtitleCueEntity>>
    @Query("SELECT * FROM subtitle_cues WHERE videoId = :videoId AND language = :language ORDER BY cueIndex ASC") suspend fun getForVideo(videoId: Long, language: String): List<SubtitleCueEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(cues: List<SubtitleCueEntity>)
    @Query("SELECT COUNT(*) FROM subtitle_cues WHERE videoId = :videoId AND language = :language") suspend fun countForVideo(videoId: Long, language: String): Int
}
