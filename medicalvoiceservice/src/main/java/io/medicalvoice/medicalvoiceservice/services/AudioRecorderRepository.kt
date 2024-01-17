package io.medicalvoice.medicalvoiceservice.services

import android.util.Log
import io.medicalvoice.medicalvoiceservice.domain.AudioRecorderConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    val audioBufferFlow = audioRecorder.audioBufferFlow

    val audioRecordingEventFlow = audioRecorder.audioRecordingEventFlow

    /** Запускает запись аудио */
    suspend fun startRecording(
        audioRecorderConfig: AudioRecorderConfig
    ) = withContext(coroutineContext) {

        Log.i(
            AudioRecorder.TAG,
            "(${this@AudioRecorderRepository::class.simpleName}) Start recording"
        )

        audioRecorder.startRecording(audioRecorderConfig)
    }

    /** Останавливает запись аудио */
    fun stopRecording() {

        Log.i(
            AudioRecorder.TAG,
            "(${this@AudioRecorderRepository::class.simpleName}) Stop recording"
        )

        audioRecorder.stopAudioRecording()
    }
}