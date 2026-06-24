package com.cactus.app.data.model
data class SubtitleCue(val id: Long = 0L, val videoId: Long, val language: SubtitleLanguage, val cueIndex: Int, val startMs: Long, val endMs: Long, val text: String, val pronunciation: String? = null)
