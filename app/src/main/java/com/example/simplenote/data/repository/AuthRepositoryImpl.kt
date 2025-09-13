package com.example.simplenote.data.repository

import androidx.security.crypto.EncryptedSharedPreferences
import com.example.simplenote.data.api.AuthApi
import com.example.simplenote.data.api.model.*
import com.example.simplenote.domain.repository.AuthRepository
import com.example.simplenote.domain.repository.AuthResult
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val prefs: EncryptedSharedPreferences
) : AuthRepository {

    companion object {
        private const val KEY_ACCESS  = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_SCHEME  = "auth_scheme"
    }

    private fun saveTokens(access: String, refresh: String?, scheme: String) {
        prefs.edit().putString(KEY_ACCESS, access).apply()
        if (refresh != null) prefs.edit().putString(KEY_REFRESH, refresh).apply()
        prefs.edit().putString(KEY_SCHEME, scheme).apply()
    }

    private fun httpErrorMessage(e: HttpException): String {
        val raw = e.response()?.errorBody()?.string().orEmpty()
        val msg = try {
            if (raw.isNotBlank()) {
                val jo = JSONObject(raw)
                when {
                    jo.has("detail") -> jo.optString("detail")
                    jo.has("non_field_errors") -> jo.optJSONArray("non_field_errors")?.optString(0)
                    jo.has("username") -> jo.optJSONArray("username")?.optString(0)
                    jo.has("password") -> jo.optJSONArray("password")?.optString(0)
                    jo.has("email")    -> jo.optJSONArray("email")?.optString(0)
                    else -> raw
                }
            } else e.message()
        } catch (_: Throwable) { if (raw.isNotBlank()) raw else e.message() }
        return "HTTP ${e.code()}: ${msg ?: "Server error"}"
    }

    override suspend fun login(username: String, password: String, scheme: String): AuthResult =
        try {
            val r = api.createToken(TokenRequest(username, password))
            saveTokens(r.access, r.refresh, scheme)
            AuthResult.Success("Login successful")
        } catch (e: HttpException) {
            AuthResult.Error(httpErrorMessage(e))
        } catch (t: Throwable) {
            AuthResult.Error(t.localizedMessage ?: "Login failed")
        }

    override suspend fun register(
        username: String,
        password: String,
        email: String,
        firstName: String?,
        lastName: String?,
        scheme: String // Ignored for now; we do not auto-login after registration
    ): AuthResult {
        return try {
            api.register(RegisterRequest(username, password, email, firstName, lastName))
            // Note: Auto-login and saveTokens removed
            AuthResult.Success("Registration successful. Please log in.")
        } catch (e: HttpException) {
            AuthResult.Error(httpErrorMessage(e))
        } catch (t: Throwable) {
            AuthResult.Error(t.localizedMessage ?: "Register failed")
        }
    }


    override suspend fun refresh(): AuthResult {
        val refresh = prefs.getString(KEY_REFRESH, null) ?: return AuthResult.Error("No refresh token")
        return try {
            val r = api.refreshToken(RefreshRequest(refresh))
            saveTokens(r.access, null, currentScheme())
            AuthResult.Success("Access refreshed")
        } catch (e: HttpException) {
            AuthResult.Error(httpErrorMessage(e))
        } catch (t: Throwable) {
            AuthResult.Error(t.localizedMessage ?: "Refresh failed")
        }
    }

    override fun logout() {
        prefs.edit().remove(KEY_ACCESS).remove(KEY_REFRESH).apply()
    }

    override fun isLoggedIn(): Boolean = !accessToken().isNullOrBlank()
    override fun currentScheme(): String = prefs.getString(KEY_SCHEME, "Bearer") ?: "Bearer"
    override fun setScheme(s: String) { prefs.edit().putString(KEY_SCHEME, s).apply() }
    override fun accessToken(): String? = prefs.getString(KEY_ACCESS, null)
    override fun refreshToken(): String? = prefs.getString(KEY_REFRESH, null)
    override suspend fun userInfo() = runCatching { api.userInfo() }
    override suspend fun changePassword(old: String, new: String): AuthResult =
        try {
            api.changePassword(ChangePasswordRequest(old, new))
            AuthResult.Success("Password changed")
        } catch (e: HttpException) {
            AuthResult.Error(httpErrorMessage(e))
        } catch (t: Throwable) {
            AuthResult.Error(t.localizedMessage ?: "Change password failed")
        }
}
