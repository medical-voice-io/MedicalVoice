package io.medicalvoice.medicalvoiceservice.services

import android.util.Log
import io.medicalvoice.medicalvoiceservice.services.events.Event
import io.medicalvoice.medicalvoiceservice.services.extensions.cancelChildrenAndJoin
import io.medicalvoice.medicalvoiceservice.services.extensions.contextJob
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

class AudioRecorderInteractor(
    private val audioRecorderRepository: AudioRecorderRepository
) : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Dispatchers.IO + SupervisorJob() + CoroutineName("AudioRecorderInteractor")

    private val _audioBufferFlow = MutableSharedFlow<ShortArray>()
    val audioBufferFlow = _audioBufferFlow.asSharedFlow()

    private val _audioRecordingEventFlow = MutableSharedFlow<Event>()
    val audioRecordingEventFlow = _audioRecordingEventFlow.asSharedFlow()

    init {
        launch {
            audioRecorderRepository.audioBufferFlow
                .shareIn(this, started = SharingStarted.Eagerly, replay = 1)
                .collect(_audioBufferFlow::emit)
        }
        launch {
            audioRecorderRepository.audioRecordingEventFlow
                .shareIn(this, started = SharingStarted.Eagerly, replay = 1)
                .collect(_audioRecordingEventFlow::emit)
        }
    }

    private val hasRunningJob: Boolean
        get() = contextJob.children.any(Job::isActive)

    suspend fun startRecording() {
        chancelRunningJob()
        withContext(coroutineContext) {

            Log.i(
                AudioRecorder.TAG,
                "(${this@AudioRecorderInteractor::class.simpleName}) Start recording"
            )
            audioRecorderRepository.startRecording()
        }
    }

    suspend fun stopRecording() {
        Log.i(AudioRecorder.TAG, "(${this::class.simpleName}) Stop recording")
        audioRecorderRepository.stopRecording()
    }

    private suspend fun chancelRunningJob() = withContext(coroutineContext) {
        if (hasRunningJob) contextJob.cancelChildrenAndJoin()
    }
}