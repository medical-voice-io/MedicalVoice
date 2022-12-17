package io.medicalvoice.medicalvoiceservice.services

import android.util.Log
import io.medicalvoice.medicalvoiceservice.services.events.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Репозиторий, который работает с [AudioRecorder]
 *
 * @property audioRecorder класс, который работает с AudioRecord
 */
class AudioRecorderRepository @Inject constructor(
    private val audioRecorder: AudioRecorder
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

    /** Запускает запись аудио */
    suspend fun startRecording() = withContext(coroutineContext) {

        Log.i(
            AudioRecorder.TAG,
            "(${this@AudioRecorderRepository::class.simpleName}) Start recording"
        )

        audioRecorder.startRecording()
    }

    /** Останавливает запись аудио */
    suspend fun stopRecording() {

        Log.i(
            AudioRecorder.TAG,
            "(${this@AudioRecorderRepository::class.simpleName}) Stop recording"
        )

        audioRecorder.stopAudioRecording()
    }
}