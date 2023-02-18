package io.medicalvoice.medicalvoiceservice.services

import android.annotation.SuppressLint
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import io.medicalvoice.medicalvoiceservice.domain.AudioRecorderConfig
import io.medicalvoice.medicalvoiceservice.services.dispatchers.AudioRecordDispatcher
import io.medicalvoice.medicalvoiceservice.services.events.Event
import io.medicalvoice.medicalvoiceservice.services.events.StartRecordingEvent
import io.medicalvoice.medicalvoiceservice.services.events.StopRecordingEvent
import io.medicalvoice.medicalvoiceservice.services.exceptions.CreateAudioRecordException
import io.medicalvoice.medicalvoiceservice.utils.retry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Класс для работы с AudioRecord.
 * Может запускать и останавливать запись аудио с микрофона
 */
class AudioRecorder @Inject constructor() : CoroutineScope {
    override val coroutineContext: CoroutineContext = AudioRecordDispatcher + Job()

    private var bufferSize: Int = 0

    private val _audioBufferFlow = MutableSharedFlow<ShortArray>()
    val audioBufferFlow = _audioBufferFlow.asSharedFlow()

    private val _audioRecordingEventFlow = MutableSharedFlow<Event>()
    val audioRecordingEventFlow = _audioRecordingEventFlow.asSharedFlow()

    /** Запуск корутины, которая записывает аудио с микрофона */
    suspend fun startRecording(
        audioRecorderConfig: AudioRecorderConfig
    ) = withContext(coroutineContext) {
        lateinit var audioRecorder: AudioRecord

        try {
            Log.i(TAG, "Start recording")

            val countAttempts = 5
            retry(countAttempts, delay = 300) {
                audioRecorder = createRecorder(audioRecorderConfig)

                audioRecorder.startRecording()

                val buffer = ShortArray(bufferSize)

                _audioRecordingEventFlow.emit(StartRecordingEvent())

                loop@ while (isActive) {
                    val shortsRead = audioRecorder.read(buffer, 0, buffer.size)
                    when {
                        shortsRead <= 0 -> {
                            audioRecorder.stop()
                            audioRecorder.release()
                            throw IOException("Read $shortsRead shorts from audioRecorder")
                        }
                        else -> {
                            _audioBufferFlow.emit(buffer.copyOf())
                        }
                    }
                }

                Log.i(TAG, "Цикл while завершился!")

                audioRecorder.stop()
                audioRecorder.release()
            }
        } catch (error: CancellationException) {
            // Ignore
        } catch (error: Throwable) {
            currentCoroutineContext().cancel()
            Log.e(TAG, "Uncaught AudioRecord exception", error)
        } finally {
            withContext(NonCancellable) {
                Log.i(TAG, "finally")
                audioRecorder.stop()
                audioRecorder.release()
                _audioRecordingEventFlow.emit(StopRecordingEvent())
            }
        }
    }

    /** Останавливает корутину записи аудио */
    suspend fun stopAudioRecording() {

        Log.i(TAG, "(${this@AudioRecorder::class.simpleName}) Stop recording")

        coroutineContext.job.cancelAndJoin()
    }

    /** Создает инстанс AudioRecord */
    @SuppressLint("MissingPermission")
    fun createRecorder(audioRecorderConfig: AudioRecorderConfig): AudioRecord {
        bufferSize = with(audioRecorderConfig) {
            AudioRecord.getMinBufferSize(
                sampleRate.value,
                channelConfig.value,
                audioFormat.value
            )
        }
        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
            val recorder = with(audioRecorderConfig) {
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate.value,
                    channelConfig.value,
                    audioFormat.value,
                    bufferSize
                )
            }
            if (recorder.state == AudioRecord.STATE_INITIALIZED) {
                return recorder
            } else {
                recorder.release()
            }
        }
        throw CreateAudioRecordException()
    }

    companion object {
        const val TAG = "MY_TAG"
    }
}