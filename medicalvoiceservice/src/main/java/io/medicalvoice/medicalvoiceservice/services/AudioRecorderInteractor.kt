package io.medicalvoice.medicalvoiceservice.services

import android.util.Log
import io.medicalvoice.medicalvoiceservice.services.events.Event
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * UseCase прослушивания AudioRecord
 *
 * @property audioRecorderRepository репозиторий, который управляет рекордером звука
 */
class AudioRecorderInteractor @Inject constructor(
    private val audioRecorderRepository: AudioRecorderRepository
) : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Dispatchers.IO + Job() + CoroutineName("AudioRecorderInteractor")

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

    /** Старт запись аудио */
    suspend fun startRecording() {
        withContext(coroutineContext) {

            Log.i(
                AudioRecorder.TAG,
                "(${this@AudioRecorderInteractor::class.simpleName}) Start recording"
            )
            audioRecorderRepository.startRecording()
        }
    }

    /** Остановка записи аудио */
    suspend fun stopRecording() {
        Log.i(AudioRecorder.TAG, "(${this::class.simpleName}) Stop recording")
        audioRecorderRepository.stopRecording()
    }
}