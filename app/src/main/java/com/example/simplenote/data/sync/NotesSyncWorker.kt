package com.example.simplenote.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.simplenote.domain.repository.NoteRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NotesSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: NoteRepository
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = try {
        repo.sync()
        Result.success()
    } catch (t: Throwable) {
        Result.retry()
    }

    companion object { const val NAME = "notes-sync" }
}
