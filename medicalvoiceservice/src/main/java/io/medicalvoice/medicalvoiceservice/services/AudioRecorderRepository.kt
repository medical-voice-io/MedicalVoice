package io.medicalvoice.medicalvoiceservice.services

import android.util.Log
import io.medicalvoice.medicalvoiceservice.services.AudioRecorder
import io.medicalvoice.medicalvoiceservice.services.events.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class AudioRecorderRepository(
    private val audioRecorder: AudioRecorder = AudioRecorder()
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val _audioBufferFlow = MutableSharedFlow<ShortArray>()
    val audioBufferFlow = _audioBufferFlow.asSharedFlow()

    private val _audioRecordingEventFlow = MutableSharedFlow<Event>()
    val audioRecordingEventFlow = _audioRecordingEventFlow.asSharedFlow()

    init {
        launch {
            audioRecorder.audioBufferFlow
                .shareIn(this, started = SharingStarted.Eagerly, replay = 1)
                .collect(_audioBufferFlow::emit)
        }
        launch {
            audioRecorder.audioRecordingEventFlow
                .shareIn(this, started = SharingStarted.Eagerly, replay = 1)
                .collect(_audioRecordingEventFlow::emit)
        }
    }

    suspend fun startRecording() = withContext(coroutineContext) {

        Log.i(
            AudioRecorder.TAG,
            "(${this@AudioRecorderRepository::class.simpleName}) Start recording"
        )

        audioRecorder.startRecording()
    }

    suspend fun stopRecording() {

        Log.i(
            AudioRecorder.TAG,
            "(${this@AudioRecorderRepository::class.simpleName}) Start recording"
        )

        audioRecorder.stopAudioRecording()
    }
}