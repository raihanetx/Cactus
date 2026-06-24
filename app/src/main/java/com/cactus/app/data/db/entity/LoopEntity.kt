package com.cactus.app.data.db.entity
import androidx.room.Entity; import androidx.room.ForeignKey; import androidx.room.Index; import androidx.room.PrimaryKey
@Entity(tableName = "loops", foreignKeys = [ForeignKey(entity = VideoEntity::class, parentColumns = ["id"], childColumns = ["videoId"], onDelete = ForeignKey.CASCADE)], indices = [Index(value = ["videoId"]), Index(value = ["videoId", "createdAt"])])
data class LoopEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0L, val videoId: Long, val name: String, val startMs: Long, val endMs: Long, val loopCount: Int, val createdAt: Long, val updatedAt: Long)
