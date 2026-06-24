package com.cactus.app.data.db.entity
import androidx.room.Entity; import androidx.room.ForeignKey; import androidx.room.Index; import androidx.room.PrimaryKey
@Entity(tableName = "subtitle_cues", foreignKeys = [ForeignKey(entity = VideoEntity::class, parentColumns = ["id"], childColumns = ["videoId"], onDelete = ForeignKey.CASCADE)], indices = [Index(value = ["videoId", "language"]), Index(value = ["videoId", "language", "cueIndex"], unique = true)])
data class SubtitleCueEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0L, val videoId: Long, val language: String, val cueIndex: Int, val startMs: Long, val endMs: Long, val text: String, val pronunciation: String? = null)
