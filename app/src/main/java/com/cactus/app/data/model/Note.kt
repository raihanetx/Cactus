package com.cactus.app.data.model
data class Note(val id: Long = 0L, val videoId: Long, val positionMs: Long, val text: String, val wordCount: Int, val createdAt: Long, val updatedAt: Long)
