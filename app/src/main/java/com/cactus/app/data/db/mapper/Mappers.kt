package com.cactus.app.data.db.mapper
import com.cactus.app.data.db.entity.*; import com.cactus.app.data.model.*
fun VideoEntity.toDomain() = Video(id, mediaStoreId, displayName, filePath, sizeBytes, durationMs, dateAddedMs, audioExtractedPath, runCatching { SubtitleStatus.valueOf(subtitleStatus) }.getOrDefault(SubtitleStatus.NOT_STARTED), subtitleProgressPct, lastPlayedAt, importedAt)
fun Video.toEntity() = VideoEntity(id, mediaStoreId, displayName, filePath, sizeBytes, durationMs, dateAddedMs, audioExtractedPath, subtitleStatus.name, subtitleProgressPct, lastPlayedAt, importedAt)
fun SubtitleCueEntity.toDomain() = SubtitleCue(id, videoId, runCatching { SubtitleLanguage.valueOf(language) }.getOrDefault(SubtitleLanguage.EN), cueIndex, startMs, endMs, text, pronunciation)
fun SubtitleCue.toEntity() = SubtitleCueEntity(id, videoId, language.name, cueIndex, startMs, endMs, text, pronunciation)
fun LoopEntity.toDomain() = Loop(id, videoId, name, startMs, endMs, loopCount, createdAt, updatedAt)
fun Loop.toEntity() = LoopEntity(id, videoId, name, startMs, endMs, loopCount, createdAt, updatedAt)
fun NoteEntity.toDomain() = Note(id, videoId, positionMs, text, wordCount, createdAt, updatedAt)
fun Note.toEntity() = NoteEntity(id, videoId, positionMs, text, wordCount, createdAt, updatedAt)
