package com.example.simplenote.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // اگر از این‌ها در AppModule استفاده نمی‌کنی، می‌توانی حذف‌شان کنی
    @Provides @Singleton
    fun provideAuthInterceptor(@ApplicationContext ctx: Context): Interceptor = Interceptor { chain ->
        val token = ctx.getSharedPreferences("auth", Context.MODE_PRIVATE).getString("jwt", null)
        val req = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) header("Authorization", "Bearer $token")
        }.build()
        chain.proceed(req)
    }

    @Provides @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            // Use BASIC in debug to avoid dumping full request/response bodies in logs
            level = if (com.example.simplenote.BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BASIC
            else
                HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization")
        }


//    @Provides @Singleton
//    fun provideRetrofit(client: OkHttpClient): Retrofit =
//        Retrofit.Builder()
//            .baseUrl("https://simple.darkube.app/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(client) // از کلاینتی که AppModule می‌سازد استفاده می‌کند
//            .build()
}
