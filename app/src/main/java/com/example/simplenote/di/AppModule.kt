package com.example.simplenote.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.simplenote.data.api.AuthApi
import com.example.simplenote.data.repository.AuthRepositoryImpl
import com.example.simplenote.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://simple.darkube.app/"
    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_ACCESS  = "access_token"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_SCHEME  = "auth_scheme" // "Bearer" or "JWT"

    @Provides @Singleton
    fun provideEncryptedPrefs(@ApplicationContext ctx: Context): EncryptedSharedPreferences {
        val mk = MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        @Suppress("UNCHECKED_CAST")
        return EncryptedSharedPreferences.create(
            ctx, PREFS_NAME, mk,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    @Provides @Singleton
    fun provideSharedPreferences(prefs: EncryptedSharedPreferences): android.content.SharedPreferences = prefs

    @Provides @Singleton
    fun provideOkHttp(prefs: EncryptedSharedPreferences): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val token = prefs.getString(KEY_ACCESS, null)
            val scheme = prefs.getString(KEY_SCHEME, "Bearer") ?: "Bearer"
            val rb = chain.request().newBuilder()
            if (!token.isNullOrBlank()) rb.addHeader("Authorization", "$scheme $token")
            chain.proceed(rb.build())
        }

        val authenticator = Authenticator { _, response ->
            if (response.request.header("Authorization") == null) return@Authenticator null
            val refresh = prefs.getString(KEY_REFRESH, null) ?: return@Authenticator null
            try {
                val body = JSONObject().put("refresh", refresh)
                    .toString().toRequestBody("application/json".toMediaType())
                val req: Request = Request.Builder()
                    .url(BASE_URL + "api/auth/token/refresh/")
                    .post(body)
                    .build()
                val simple = OkHttpClient()
                val res = simple.newCall(req).execute()
                if (!res.isSuccessful) return@Authenticator null
                val newAccess = JSONObject(res.body?.string().orEmpty()).optString("access")
                if (newAccess.isNullOrBlank()) return@Authenticator null
                prefs.edit().putString(KEY_ACCESS, newAccess).apply()
                val scheme = prefs.getString(KEY_SCHEME, "Bearer") ?: "Bearer"
                response.request.newBuilder()
                    .header("Authorization", "$scheme $newAccess")
                    .build()
            } catch (_: Throwable) { null }
        }

        val logging = HttpLoggingInterceptor().apply {
            // Avoid logging full bodies to protect user data; BASIC logs method/URL/status only
            level = if (com.example.simplenote.BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
            // Redact sensitive headers
            redactHeader("Authorization")
        }

        return OkHttpClient.Builder()
            .authenticator(authenticator)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    /** ← فقط همین یک Retrofit در کل پروژه */
    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL) // اسلش پایانی
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    // توجه: اینجا دیگر provideAuthApi نداریم؛ در AuthApiModule ساخته می‌شود.

    @Provides @Singleton
    fun provideAuthRepository(
        api: AuthApi, // از AuthApiModule می‌آید
        prefs: EncryptedSharedPreferences
    ): AuthRepository = AuthRepositoryImpl(api, prefs)
}
