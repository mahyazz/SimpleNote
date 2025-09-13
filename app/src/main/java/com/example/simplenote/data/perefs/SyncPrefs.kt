package com.example.simplenote.data.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncPrefs @Inject constructor(
    @ApplicationContext ctx: Context
) {
    private val sp = ctx.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
    fun getLastSync(): Long = sp.getLong("notes_last_sync", 0L)
    fun setLastSync(ts: Long) { sp.edit().putLong("notes_last_sync", ts).apply() }
}
