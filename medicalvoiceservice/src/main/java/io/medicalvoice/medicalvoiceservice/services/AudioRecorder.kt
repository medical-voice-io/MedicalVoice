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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Класс для работы с AudioRecord.
 * Может запускать и останавливать запись аудио с микрофона
 */
class AudioRecorder @Inject constructor() : CoroutineScope {
    override val coroutineContext: CoroutineContext = AudioRecordDispatcher + Job()

    // private var bufferSize: Int = 0

    private val _audioBufferFlow = MutableSharedFlow<ShortArray>()
    val audioBufferFlow = _audioBufferFlow.asSharedFlow()

    private val _audioRecordingEventFlow = MutableStateFlow<Event>(StopRecordingEvent)
    val audioRecordingEventFlow = _audioRecordingEventFlow.asStateFlow()

    /** Запуск корутины, которая записывает аудио с микрофона */
    suspend fun startRecording(
        audioRecorderConfig: AudioRecorderConfig,
        countNumberForFft: Int
    ) = withContext(coroutineContext) {
        val countAttempts = 5
        retry(countAttempts, delay = 300) {
            runCatching {
                createRecorder(audioRecorderConfig, countNumberForFft)
            }.onSuccess { (audioRecord, bufferSize) ->
                // val bufferSizeFoFft = getCountNumberForFft(
                //     frequency = audioRecorderConfig.sampleRate.value
                // )
                try {
                    _audioRecordingEventFlow.emit(StartRecordingEvent)
                    audioRecord.startRecording()

                    // val buffer = ShortArray(bufferSize)
                    val buffer = ShortArray(bufferSize)

                    loop@ while (isActive) {
                        val shortsRead = audioRecord.read(buffer, 0, buffer.size)
                        if (shortsRead > 0) {
                            _audioBufferFlow.emit(buffer.copyOf())
                        } else {
                            audioRecord.release()
                            throw IOException("Read $shortsRead shorts from audioRecorder")
                        }
                    }

                    Log.i(TAG, "Цикл while завершился!")

                    audioRecord.release()
                } catch (error: CancellationException) {
                    // Ignore
                } catch (error: Throwable) {
                    currentCoroutineContext().cancel()
                    Log.e(TAG, "Uncaught AudioRecord exception", error)
                } finally {
                    withContext(NonCancellable) {
                        Log.i(TAG, "finally")
                        audioRecord.release()
                        _audioRecordingEventFlow.emit(StopRecordingEvent)
                    }
                }
            }.onFailure {
                Log.i(TAG, "Не удалось создать AudioRecord")
                _audioRecordingEventFlow.emit(StopRecordingEvent)
            }
        }
    }

    /** Останавливает корутину записи аудио */
    fun stopAudioRecording() {

        Log.i(TAG, "(${this@AudioRecorder::class.simpleName}) Stop recording")

        coroutineContext.job.cancel()
    }

    /** Создает инстанс AudioRecord */
    @SuppressLint("MissingPermission")
    fun createRecorder(
        audioRecorderConfig: AudioRecorderConfig,
        countNumberForFft: Int,
    ): Pair<AudioRecord, Int> {
        val minIteration = 20
        // val bufferSize = with(audioRecorderConfig) {
        //     AudioRecord.getMinBufferSize(
        //         sampleRate.value,
        //         channelConfig.value,
        //         audioFormat.value
        //     ) * 10
        // }
        val bufferSize = countNumberForFft * minIteration
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
                return recorder to bufferSize
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