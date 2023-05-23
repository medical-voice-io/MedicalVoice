package io.medicalvoice.medicalvoiceservice.services.usecases.record

import android.util.Log
import io.medicalvoice.medicalvoiceservice.domain.AudioRecorderConfig
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * UseCase прослушивания AudioRecord
 *
 * @property audioRecorderRepository репозиторий, который управляет рекордером звука
 */
class AudioRecorderUseCase @Inject constructor(
    private val audioRecorderRepository: AudioRecorderRepository
) : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Dispatchers.IO + Job() + CoroutineName("AudioRecorderUseCase")

    val audioBufferFlow = audioRecorderRepository.audioBufferFlow
    val audioRecordingEventFlow = audioRecorderRepository.audioRecordingEventFlow

    /** Старт запись аудио */
    suspend fun startRecording(audioRecorderConfig: AudioRecorderConfig) {
        withContext(coroutineContext) {

            Log.i(
                AudioRecorder.TAG,
                "(${this@AudioRecorderUseCase::class.simpleName}) Start recording"
            )
            audioRecorderRepository.startRecording(audioRecorderConfig)
        }
    }

    /** Остановка записи аудио */
    suspend fun stopRecording() {
        Log.i(AudioRecorder.TAG, "(${this::class.simpleName}) Stop recording")
        audioRecorderRepository.stopRecording()
    }
}