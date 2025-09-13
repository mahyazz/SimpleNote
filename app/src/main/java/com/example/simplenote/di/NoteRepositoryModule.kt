package com.example.simplenote.di

import android.content.Context
import androidx.work.WorkManager
import com.example.simplenote.data.repository.NoteRepositoryImpl
import com.example.simplenote.domain.repository.NoteRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NoteRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository
}

@Module
@InstallIn(SingletonComponent::class)
object WorkModule {
    @Provides
    fun provideWorkManager(@ApplicationContext ctx: Context): WorkManager =
        WorkManager.getInstance(ctx)
}
