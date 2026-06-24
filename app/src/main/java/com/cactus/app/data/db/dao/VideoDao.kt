package com.cactus.app.data.db.dao
import androidx.room.*
import com.cactus.app.data.db.entity.VideoEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY importedAt DESC") fun observeAllByNewest(): Flow<List<VideoEntity>>
    @Query("SELECT * FROM videos ORDER BY sizeBytes DESC") fun observeAllBySizeDesc(): Flow<List<VideoEntity>>
    @Query("SELECT * FROM videos ORDER BY sizeBytes ASC") fun observeAllBySizeAsc(): Flow<List<VideoEntity>>
    @Query("SELECT * FROM videos WHERE subtitleStatus = 'READY' ORDER BY importedAt DESC") fun observeWithSubtitles(): Flow<List<VideoEntity>>
    @Query("SELECT * FROM videos WHERE subtitleStatus != 'READY' ORDER BY importedAt DESC") fun observeWithoutSubtitles(): Flow<List<VideoEntity>>
    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1") suspend fun getById(id: Long): VideoEntity?
    @Query("SELECT * FROM videos WHERE mediaStoreId = :mediaStoreId LIMIT 1") suspend fun getByMediaStoreId(mediaStoreId: Long): VideoEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(video: VideoEntity): Long
    @Update suspend fun update(video: VideoEntity)
    @Query("UPDATE videos SET audioExtractedPath = :path WHERE id = :videoId") suspend fun setAudioExtractedPath(videoId: Long, path: String)
    @Query("UPDATE videos SET subtitleStatus = :status, subtitleProgressPct = :pct WHERE id = :videoId") suspend fun setSubtitleStatus(videoId: Long, status: String, pct: Int)
    @Query("UPDATE videos SET lastPlayedAt = :timestamp WHERE id = :videoId") suspend fun setLastPlayedAt(videoId: Long, timestamp: Long)
}
