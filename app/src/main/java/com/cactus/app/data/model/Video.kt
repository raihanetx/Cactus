package com.cactus.app.data.model
data class Video(val id: Long = 0L, val mediaStoreId: Long, val displayName: String, val filePath: String, val sizeBytes: Long, val durationMs: Long, val dateAddedMs: Long, val audioExtractedPath: String? = null, val subtitleStatus: SubtitleStatus = SubtitleStatus.NOT_STARTED, val subtitleProgressPct: Int = 0, val lastPlayedAt: Long? = null, val importedAt: Long)
