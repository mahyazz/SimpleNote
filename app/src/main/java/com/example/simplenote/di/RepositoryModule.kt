package com.example.simplenote.di

import com.example.simplenote.data.repository.AuthRepositoryImpl
import com.example.simplenote.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {


}
