package com.cactus.app.data.db
import androidx.room.*; import com.cactus.app.data.db.dao.*; import com.cactus.app.data.db.entity.*
@Database(entities = [VideoEntity::class, SubtitleCueEntity::class, LoopEntity::class, NoteEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() { abstract fun videoDao(): VideoDao; abstract fun subtitleCueDao(): SubtitleCueDao; abstract fun loopDao(): LoopDao; abstract fun noteDao(): NoteDao }
