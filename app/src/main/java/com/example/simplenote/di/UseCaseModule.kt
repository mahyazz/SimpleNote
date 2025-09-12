package com.example.simplenote.di

import com.example.simplenote.core.validation.PasswordValidator
import com.example.simplenote.domain.repository.AuthRepository
import com.example.simplenote.domain.usecase.ChangePasswordUseCase
import com.example.simplenote.domain.usecase.RegisterUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun providePasswordValidator(): PasswordValidator = PasswordValidator()

    @Provides
    @Singleton
    fun provideRegisterUseCase(
        repo: AuthRepository,
        validator: PasswordValidator
    ): RegisterUseCase = RegisterUseCase(repo, validator)

    @Provides
    @Singleton
    fun provideChangePasswordUseCase(
        repo: AuthRepository,
        validator: PasswordValidator
    ): ChangePasswordUseCase = ChangePasswordUseCase(repo, validator)
}
