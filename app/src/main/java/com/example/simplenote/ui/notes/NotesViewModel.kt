@file:Suppress("RedundantQualifierName")

package com.example.simplenote.ui.notes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.domain.model.Note
import com.example.simplenote.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
import androidx.security.crypto.EncryptedSharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.example.simplenote.domain.model.NoteInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.flatMapLatest
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi



@HiltViewModel
open class NotesViewModel @Inject constructor(
    private val repo: NoteRepository,
    private val prefs: android.content.SharedPreferences,
) : ViewModel() {


    private val _filter = kotlinx.coroutines.flow.MutableStateFlow<com.example.simplenote.domain.model.NoteFilter?>(null)
    open val filter: StateFlow<com.example.simplenote.domain.model.NoteFilter?> = _filter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    open val paged: Flow<PagingData<Note>> =
        filter.flatMapLatest { f ->
            if (f == null) repo.pagedNotes()
            else repo.pagedNotesFiltered(f)
        }.cachedIn(viewModelScope)



    fun setFilter(f: com.example.simplenote.domain.model.NoteFilter?) {
        _filter.value = f
    }

    fun applyFilter(
        title: String?,
        description: String?,
        updatedGteMillis: Long?,
        updatedLteMillis: Long?
    ) {
        val f = com.example.simplenote.domain.model.NoteFilter(
            title = title?.takeIf { it.isNotBlank() },
            description = description?.takeIf { it.isNotBlank() },
            updatedGteMillis = updatedGteMillis,
            updatedLteMillis = updatedLteMillis
        )
        _filter.value = f
    }

    fun clearFilter() {
        _filter.value = null
    }

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar.asStateFlow()


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
        else -> localizedMessage ?: "خطای ناشناخته"
    }

    fun addRemote(title: String, description: String) = viewModelScope.launch {
        try {
            isBusy.value = true
            withContext(Dispatchers.IO) { repo.createRemote(title, description) }
            snackbarMessage.value = "Created on server"
        } catch (t: Throwable) {
            snackbarMessage.value = t.message ?: "Failed"
        } finally {
            isBusy.value = false
        }
    }

    fun addBulk(items: List<NoteInput>) = viewModelScope.launch {


        val ok = runCatching {
            withContext(Dispatchers.IO) { repo.bulkCreateRemote(items) }
        }.isSuccess
        if (!ok) {
            withContext(Dispatchers.IO) { repo.bulkCreateLocal(items) }
        }
    }
    // Secondary constructor for Compose previews
    protected constructor(): this(PREVIEW_REPO, SimplePrefs())

    companion object {
        private const val TOKEN_KEY = "access_token"
        private const val SCHEME_KEY = "auth_scheme"
        // Minimal in-memory SharedPreferences for previews
        private class SimplePrefs : android.content.SharedPreferences {
            private val map = mutableMapOf<String, Any?>()
            override fun getAll(): MutableMap<String, *> = map
            override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
            override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = map[key] as? MutableSet<String> ?: defValues
            override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
            override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
            override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
            override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
            override fun contains(key: String?): Boolean = map.containsKey(key)
            override fun edit(): android.content.SharedPreferences.Editor = object : android.content.SharedPreferences.Editor {
                override fun putString(key: String?, value: String?): android.content.SharedPreferences.Editor { if (key != null) map[key] = value; return this }
                override fun putStringSet(key: String?, values: MutableSet<String>?): android.content.SharedPreferences.Editor { if (key != null) map[key] = values; return this }
                override fun putInt(key: String?, value: Int): android.content.SharedPreferences.Editor { if (key != null) map[key] = value; return this }
                override fun putLong(key: String?, value: Long): android.content.SharedPreferences.Editor { if (key != null) map[key] = value; return this }
                override fun putFloat(key: String?, value: Float): android.content.SharedPreferences.Editor { if (key != null) map[key] = value; return this }
                override fun putBoolean(key: String?, value: Boolean): android.content.SharedPreferences.Editor { if (key != null) map[key] = value; return this }
                override fun remove(key: String?): android.content.SharedPreferences.Editor { if (key != null) map.remove(key); return this }
                override fun clear(): android.content.SharedPreferences.Editor { map.clear(); return this }
                override fun commit(): Boolean = true
                override fun apply() {}
            }
            override fun registerOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}
            override fun unregisterOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}
        }

        private val PREVIEW_REPO: com.example.simplenote.domain.repository.NoteRepository = object : com.example.simplenote.domain.repository.NoteRepository {
            override fun observeNotes(): kotlinx.coroutines.flow.Flow<List<com.example.simplenote.domain.model.Note>> = kotlinx.coroutines.flow.flowOf(emptyList())
            override suspend fun upsertLocal(note: com.example.simplenote.domain.model.Note) {}
            override suspend fun deleteLocal(id: String) {}
            override suspend fun refresh(): com.example.simplenote.domain.repository.RefreshResult = com.example.simplenote.domain.repository.RefreshResult(0, 0, ok = true)
            override suspend fun sync(): com.example.simplenote.domain.repository.SyncResult = com.example.simplenote.domain.repository.SyncResult(0,0,0, ok = true)
            override suspend fun ping(): com.example.simplenote.domain.repository.PingResult = com.example.simplenote.domain.repository.PingResult(ok = true)
            override fun pagedNotes(pageSize: Int): kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<com.example.simplenote.domain.model.Note>> = kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.empty())
            override suspend fun createRemote(title: String, description: String): com.example.simplenote.domain.model.Note = com.example.simplenote.domain.model.Note(id = "", remoteId = null, title = title, description = description, createdAt = 0, updatedAt = 0)
            override suspend fun fetchRemote(remoteId: Int): com.example.simplenote.domain.model.Note = com.example.simplenote.domain.model.Note(id = "", remoteId = remoteId, title = "", description = "", createdAt = 0, updatedAt = 0)
            override suspend fun updateRemote(remoteId: Int, title: String, description: String): com.example.simplenote.domain.model.Note = fetchRemote(remoteId)
            override suspend fun getLocalByRemoteId(remoteId: Int): com.example.simplenote.domain.model.Note? = null
            override suspend fun updateLocal(id: String, title: String, description: String) {}
            override suspend fun createLocal(title: String, description: String): com.example.simplenote.domain.model.Note = com.example.simplenote.domain.model.Note(id = "", remoteId = null, title = title, description = description, createdAt = 0, updatedAt = 0)
            override suspend fun getLocalByLocalId(id: String): com.example.simplenote.domain.model.Note? = null
            override suspend fun patchLocal(id: String, title: String?, description: String?) {}
            override suspend fun patchRemote(remoteId: Int, title: String?, description: String?): com.example.simplenote.domain.model.Note = fetchRemote(remoteId)
            override suspend fun deleteRemote(remoteId: Int): Boolean = true
            override suspend fun bulkCreateRemote(items: List<com.example.simplenote.domain.model.NoteInput>): List<com.example.simplenote.domain.model.Note> = emptyList()
            override suspend fun bulkCreateLocal(items: List<com.example.simplenote.domain.model.NoteInput>): List<com.example.simplenote.domain.model.Note> = emptyList()
            override fun pagedNotesFiltered(filter: com.example.simplenote.domain.model.NoteFilter, pageSize: Int): kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<com.example.simplenote.domain.model.Note>> = kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.empty())
        }
    }
}
