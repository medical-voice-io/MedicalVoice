package io.medicalvoice.medicalvoiceservice.services.dispatchers

import android.os.Process
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

private const val AUDIO_RECORD_THREAD = "AudioRecord thread"

/** Диспатчер корутины для записи аудио */
private val dispatcher = Executors.newSingleThreadExecutor { runnable ->
    object : Thread(runnable, AUDIO_RECORD_THREAD) {
        init {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        }
    }
}.asCoroutineDispatcher()

internal val AudioRecordDispatcher
    get() = dispatcher
