package io.medicalvoice.medicalvoiceservice.services

import android.util.Log
import io.medicalvoice.medicalvoiceservice.domain.AudioRecorderConfig
import javax.inject.Inject

/**
 * Репозиторий, который работает с [AudioRecorder]
 *
 * @property audioRecorder класс, который работает с AudioRecord
 */
class AudioRecorderRepository @Inject constructor(
    private val audioRecorder: AudioRecorder
) {

    val audioBufferFlow = audioRecorder.audioBufferFlow

    val audioRecordingEventFlow = audioRecorder.audioRecordingEventFlow

    /** Запускает запись аудио */
    suspend fun startRecording(
        audioRecorderConfig: AudioRecorderConfig
    ) {

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