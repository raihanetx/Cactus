package com.cactus.app.di
import android.content.Context; import androidx.room.Room; import com.cactus.app.data.db.AppDatabase; import com.cactus.app.data.db.dao.*
import dagger.Module; import dagger.Provides; import dagger.hilt.InstallIn; import dagger.hilt.android.qualifiers.ApplicationContext; import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "cactus.db").fallbackToDestructiveMigration().build()
    @Provides fun provideVideoDao(db: AppDatabase): VideoDao = db.videoDao()
    @Provides fun provideSubtitleCueDao(db: AppDatabase): SubtitleCueDao = db.subtitleCueDao()
    @Provides fun provideLoopDao(db: AppDatabase): LoopDao = db.loopDao()
    @Provides fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()
}
