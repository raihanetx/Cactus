package com.cactus.app.data.db
import androidx.room.TypeConverter; import com.cactus.app.data.model.*
class Converters {
    @TypeConverter fun fromSubtitleStatus(s: SubtitleStatus): String = s.name
    @TypeConverter fun toSubtitleStatus(v: String): SubtitleStatus = runCatching { SubtitleStatus.valueOf(v) }.getOrDefault(SubtitleStatus.NOT_STARTED)
    @TypeConverter fun fromSubtitleLanguage(l: SubtitleLanguage): String = l.name
    @TypeConverter fun toSubtitleLanguage(v: String): SubtitleLanguage = runCatching { SubtitleLanguage.valueOf(v) }.getOrDefault(SubtitleLanguage.EN)
}
