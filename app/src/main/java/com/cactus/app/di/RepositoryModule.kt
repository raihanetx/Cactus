package com.cactus.app.di
import com.cactus.app.data.repository.*; import dagger.Binds; import dagger.Module; import dagger.hilt.InstallIn; import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindVideoRepository(impl: VideoRepositoryImpl): VideoRepository
    @Binds @Singleton abstract fun bindSubtitleRepository(impl: SubtitleRepositoryImpl): SubtitleRepository
    @Binds @Singleton abstract fun bindLoopRepository(impl: LoopRepositoryImpl): LoopRepository
    @Binds @Singleton abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository
}
