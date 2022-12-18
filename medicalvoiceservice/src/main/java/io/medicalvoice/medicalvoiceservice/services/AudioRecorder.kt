package io.medicalvoice.medicalvoiceservice.services

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import io.medicalvoice.medicalvoiceservice.services.dispatchers.AudioRecordDispatcher
import io.medicalvoice.medicalvoiceservice.services.events.Event
import io.medicalvoice.medicalvoiceservice.services.events.StartRecordingEvent
import io.medicalvoice.medicalvoiceservice.services.events.StopRecordingEvent
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
    suspend fun startRecording() = withContext(coroutineContext) {
        lateinit var audioRecorder: AudioRecord

        try {
            Log.i(TAG, "Start recording")

            val countAttempts = 5
            retry(countAttempts, delay = 300) { attempt ->
                audioRecorder = createRecorder()
                if (audioRecorder.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "Failed to init AudioRecord, attempt: $attempt")
                    audioRecorder.release()
                    throw IOException("Failed to init AudioRecord after $countAttempts retries")
                }

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
    private fun createRecorder(): AudioRecord {
        SAMPLE_RATES.forEach { sampleRate ->
            AUDIO_FORMATS.forEach { audioFormat ->
                CHANNEL_CONFIGS.forEach { channelConfig ->
                    bufferSize = AudioRecord.getMinBufferSize(
                        sampleRate,
                        channelConfig,
                        audioFormat
                    )

                    if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                        val recorder = AudioRecord(
                            MediaRecorder.AudioSource.MIC,
                            sampleRate,
                            channelConfig,
                            audioFormat,
                            bufferSize
                        )

                        if (recorder.state == AudioRecord.STATE_INITIALIZED) {
                            return recorder
                        } else {
                            recorder.release()
                        }
                    }
                }
            }
        }
        throw IOException("Не удалось создать AudioRecord")
    }

    companion object {
        const val TAG = "MY_TAG"

        val SAMPLE_RATES = setOf(
            44100,
            22050,
            16000,
            11025,
            8000
        )
        val AUDIO_FORMATS = setOf(
            AudioFormat.ENCODING_PCM_16BIT,
            AudioFormat.ENCODING_PCM_FLOAT,
            AudioFormat.ENCODING_PCM_8BIT,
            AudioFormat.ENCODING_DEFAULT
        )
        val CHANNEL_CONFIGS = setOf(
            AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.CHANNEL_IN_MONO
        )
    }
}