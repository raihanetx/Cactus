package com.cactus.app.data.db.entity
import androidx.room.Entity; import androidx.room.Index; import androidx.room.PrimaryKey
@Entity(tableName = "videos", indices = [Index(value = ["mediaStoreId"], unique = true), Index(value = ["importedAt"]), Index(value = ["sizeBytes"]), Index(value = ["subtitleStatus"]), Index(value = ["lastPlayedAt"])])
data class VideoEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0L, val mediaStoreId: Long, val displayName: String, val filePath: String, val sizeBytes: Long, val durationMs: Long, val dateAddedMs: Long, val audioExtractedPath: String? = null, val subtitleStatus: String, val subtitleProgressPct: Int = 0, val lastPlayedAt: Long? = null, val importedAt: Long)
