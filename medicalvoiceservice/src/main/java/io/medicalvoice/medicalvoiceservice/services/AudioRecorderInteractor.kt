package io.medicalvoice.medicalvoiceservice.services

import android.util.Log
import io.medicalvoice.medicalvoiceservice.domain.AudioRecorderConfig
import io.medicalvoice.medicalvoiceservice.services.events.Event
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            audioRecorderRepository.audioBufferFlow.collect(_audioBufferFlow::emit)
        }
        launch {
            audioRecorderRepository.audioRecordingEventFlow.collect(_audioRecordingEventFlow::emit)
        }
    }

    /** Старт запись аудио */
    suspend fun startRecording(audioRecorderConfig: AudioRecorderConfig) {
        withContext(coroutineContext) {

            Log.i(
                AudioRecorder.TAG,
                "(${this@AudioRecorderInteractor::class.simpleName}) Start recording"
            )
            audioRecorderRepository.startRecording(audioRecorderConfig)
        }
    }

    /** Остановка записи аудио */
    fun stopRecording() {
        Log.i(AudioRecorder.TAG, "(${this::class.simpleName}) Stop recording")
        audioRecorderRepository.stopRecording()
    }
}