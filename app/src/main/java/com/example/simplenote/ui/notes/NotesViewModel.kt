package com.example.simplenote.ui.notes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.domain.model.Note
import com.example.simplenote.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.URL
import java.util.UUID
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.paging.ExperimentalPagingApi
import androidx.paging.cachedIn
import androidx.security.crypto.EncryptedSharedPreferences

@HiltViewModel
class NotesViewModel @Inject constructor(

    private val repo: NoteRepository,
    private val prefs: EncryptedSharedPreferences,

) : ViewModel() {

    companion object {
        private const val TOKEN_KEY = "access_token"
        private const val SCHEME_KEY = "auth_scheme" // "JWT" یا "Bearer"
    }
    @OptIn(ExperimentalPagingApi::class)
    val paged = repo.pagedNotes(20).cachedIn(viewModelScope)

    val notes = repo.observeNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<Note>())

    val snackbarMessage: MutableState<String?> = mutableStateOf(null)
    val isBusy: MutableState<Boolean> = mutableStateOf(false)

    fun consumeMessage() { snackbarMessage.value = null }

    /* ---------- Token/Scheme مدیریت ---------- */
    fun tokenStatus(): String {
        val t = prefs.getString(TOKEN_KEY, null)
        val s = prefs.getString(SCHEME_KEY, "JWT")
        val tMask = if (t.isNullOrBlank()) "MISSING" else "PRESENT (${t.take(8)}…)"
        return "Token: $tMask • Scheme: $s"
    }
    fun setToken(token: String) {
        prefs.edit().putString(TOKEN_KEY, token.trim()).apply()
        snackbarMessage.value = "Token saved. ${tokenStatus()}"
    }
    fun clearToken() {
        prefs.edit().remove(TOKEN_KEY).apply()
        snackbarMessage.value = "Token cleared. ${tokenStatus()}"
    }
    fun setScheme(scheme: String) {
        prefs.edit().putString(SCHEME_KEY, scheme).apply()
        snackbarMessage.value = "Scheme set to $scheme. ${tokenStatus()}"
    }

    /* ---------- CRUD / Sync ---------- */
    fun add(title: String, desc: String) = viewModelScope.launch {
        try {
            isBusy.value = true
            withContext(Dispatchers.IO) {
                repo.upsertLocal(
                    Note(
                        id = UUID.randomUUID().toString(),
                        remoteId = null,
                        title = title,
                        description = desc,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            snackbarMessage.value = "Saved locally. Will sync on network."
        } catch (t: Throwable) {
            Log.e("NotesVM", "add failed", t)
            snackbarMessage.value = t.userMessage()
        } finally { isBusy.value = false }
    }

    fun delete(localId: String) = viewModelScope.launch {
        try { isBusy.value = true; withContext(Dispatchers.IO) { repo.deleteLocal(localId) }
            snackbarMessage.value = "Deleted locally. Will sync on network."
        } catch (t: Throwable) { Log.e("NotesVM","delete failed",t); snackbarMessage.value = t.userMessage()
        } finally { isBusy.value = false }
    }

    fun refresh() = viewModelScope.launch {
        try { isBusy.value = true
            val r = withContext(Dispatchers.IO) { repo.refresh() }
            snackbarMessage.value = "Refresh → ${if (r.ok) "OK" else "FAILED"} (${r.message ?: ""}) • pulled=${r.pulled} • pages=${r.pages} • ${tokenStatus()}"
        } catch (t: Throwable) { Log.e("NotesVM","refresh failed",t); snackbarMessage.value = t.userMessage()
        } finally { isBusy.value = false }
    }

    fun sync() = viewModelScope.launch {
        try { isBusy.value = true
            val r = withContext(Dispatchers.IO) { repo.sync() }
            snackbarMessage.value = "Sync → ${if (r.ok) "OK" else "FAILED"} (${r.message ?: ""}) • pushed=${r.pushed} • deleted=${r.deleted} • pulled=${r.pulled} • ${tokenStatus()}"
        } catch (t: Throwable) { Log.e("NotesVM","sync failed",t); snackbarMessage.value = t.userMessage()
        } finally { isBusy.value = false }
    }

    fun ping() = viewModelScope.launch {
        try { isBusy.value = true
            val p = withContext(Dispatchers.IO) { repo.ping() }
            snackbarMessage.value = buildString {
                append("Ping → "); append(if (p.ok) "OK" else "FAILED")
                append(" • "); append(tokenStatus())
                p.httpCode?.let { append(" • HTTP "); append(it) }
                p.httpMessage?.let { append(" "); append(it) }
                p.error?.let { append(" • "); append(it) }
            }
        } catch (t: Throwable) { snackbarMessage.value = t.userMessage()
        } finally { isBusy.value = false }
    }

    /* ---------- Raw Connectivity Test (بدون Retrofit/OkHttp) ---------- */
    fun debugNetwork() = viewModelScope.launch {
        try {
            isBusy.value = true
            val details = withContext(Dispatchers.IO) {
                val url = URL("https://simple.darkube.app/api/notes/")
                val host = url.host
                val ipInfo = runCatching {
                    InetAddress.getAllByName(host).joinToString { it.hostAddress }
                }.getOrElse { "DNS error: ${it.message}" }

                val token = prefs.getString(TOKEN_KEY, null)
                val scheme = prefs.getString(SCHEME_KEY, "JWT") ?: "JWT"

                runCatching {
                    val conn = (url.openConnection() as HttpsURLConnection).apply {
                        connectTimeout = 4000
                        readTimeout = 4000
                        requestMethod = "GET"
                        if (!token.isNullOrBlank()) setRequestProperty("Authorization", "$scheme $token")
                    }
                    val code = conn.responseCode
                    val msg = conn.responseMessage
                    conn.disconnect()
                    "RAW GET → HTTP $code $msg • host=$host • ip=$ipInfo • auth=${if (token.isNullOrBlank()) "none" else "$scheme <${token.take(6)}…>"}"
                }.getOrElse { "RAW GET failed: ${it.javaClass.simpleName}: ${it.message} • host=$host • ip=$ipInfo" }
            }
            snackbarMessage.value = details
        } finally {
            isBusy.value = false
        }
    }

    private fun Throwable.userMessage(): String = when (this) {
        is retrofit2.HttpException -> "Server error: ${code()} ${message()}"
        is java.net.UnknownHostException -> "اتصال اینترنت برقرار نیست."
        is java.net.SocketTimeoutException -> "درخواست زمان‌بر شد."
        is java.time.format.DateTimeParseException -> "خطا در پارس تاریخ."
        else -> localizedMessage ?: "خطای ناشناخته"
    }
}
