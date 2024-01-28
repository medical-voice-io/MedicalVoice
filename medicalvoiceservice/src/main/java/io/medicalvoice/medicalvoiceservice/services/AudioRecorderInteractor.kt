package io.medicalvoice.medicalvoiceservice.services

import android.util.Log
import io.medicalvoice.medicalvoiceservice.domain.AudioRecorderConfig
import io.medicalvoice.medicalvoiceservice.services.events.Event
import io.shiryaev.PreprocessingUseCase
import io.shiryaev.method.Frame
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * UseCase прослушивания AudioRecord
 *
 * @property audioRecorderRepository репозиторий, который управляет рекордером звука
 * @property preprocessingUseCase предварительная обработка сигнала
 */
class AudioRecorderInteractor @Inject constructor(
    private val audioRecorderRepository: AudioRecorderRepository,
    private val preprocessingUseCase: PreprocessingUseCase
) : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Dispatchers.IO + Job() + CoroutineName("AudioRecorderInteractor")

    private val _audioBufferFlow = MutableSharedFlow<List<Frame>>()
    val audioBufferFlow = _audioBufferFlow.asSharedFlow()

    private val _audioRecordingEventFlow = MutableSharedFlow<Event>()
    val audioRecordingEventFlow = _audioRecordingEventFlow.asSharedFlow()

    init {
        launch {
            audioRecorderRepository.audioRecordingEventFlow.collect(_audioRecordingEventFlow::emit)
        }
    }

    /** Старт запись аудио */
    suspend fun startRecording(audioRecorderConfig: AudioRecorderConfig) {
        audioRecorderRepository.audioBufferFlow
            .map { audioAmplitudes ->
                preprocessingUseCase(
                    frequency = audioRecorderConfig.sampleRate.value,
                    amplitudes = audioAmplitudes.map { it.toDouble() }
                )
            }
            .onEach(_audioBufferFlow::emit)
            .launchIn(this)

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