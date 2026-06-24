package com.cactus.app.data.db.entity
import androidx.room.Entity; import androidx.room.ForeignKey; import androidx.room.Index; import androidx.room.PrimaryKey
@Entity(tableName = "notes", foreignKeys = [ForeignKey(entity = VideoEntity::class, parentColumns = ["id"], childColumns = ["videoId"], onDelete = ForeignKey.CASCADE)], indices = [Index(value = ["videoId"]), Index(value = ["videoId", "positionMs"])])
data class NoteEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0L, val videoId: Long, val positionMs: Long, val text: String, val wordCount: Int, val createdAt: Long, val updatedAt: Long)
