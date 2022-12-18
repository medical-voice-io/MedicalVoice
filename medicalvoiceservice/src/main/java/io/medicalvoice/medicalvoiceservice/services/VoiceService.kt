package io.medicalvoice.medicalvoiceservice.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import io.medicalvoice.medicalvoiceservice.logger.FileLogger
import io.medicalvoice.medicalvoiceservice.services.binders.MedicalVoiceBinder
import io.medicalvoice.medicalvoiceservice.services.events.Event
import io.medicalvoice.medicalvoiceservice.services.events.StopRecordingEvent
import io.medicalvoice.medicalvoiceservice.services.extensions.startForegroundAndShowNotification
import io.medicalvoice.medicalvoiceservice.services.extensions.stopForeground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Сервис для записи аудио из микрофона в фоновом режиме
 *
 * @property audioRecorderInteractor usercase старта/остановки записи аудио
 */
class VoiceService(
    private val audioRecorderInteractor: AudioRecorderInteractor = AudioRecorderInteractor(
        audioRecorderRepository = AudioRecorderRepository()
    )
) : Service(), CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO

    private val _audioRecordingEventFlow = MutableSharedFlow<Event>()

    // Массив куда будем собирать записанные с микрофона данные
    private val soundArray: ArrayList<Short> = arrayListOf()

    init {
        launch {
            audioRecorderInteractor.audioBufferFlow
                .shareIn(this, started = SharingStarted.Eagerly, replay = 1)
                // При каждом полученном значении кладем в массив
                .onEach {
                    soundArray.addAll(it.toList())
                }
                .collect { buffer ->
                    Log.i(AudioRecorder.TAG, buffer.map { it.toString() }.toString())
                }
        }
        launch {
            audioRecorderInteractor.audioRecordingEventFlow
                .shareIn(this, started = SharingStarted.Eagerly, replay = 1)
                .collect { event ->
                    if (event is StopRecordingEvent) stopService()
                    _audioRecordingEventFlow.emit(event)
                }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "$TAG onStartCommand")

        startForegroundAndShowNotification()

        launch(coroutineContext) {
            audioRecorderInteractor.startRecording()
            stopSelf()
        }
        return START_STICKY
    }

    override fun stopService(name: Intent?): Boolean {
        Log.i(TAG, "$TAG stopService")
        launch(coroutineContext) {
            audioRecorderInteractor.stopRecording()
        }
        return super.stopService(name)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "$TAG onUnbind")
        // Записываем полученнеы данные с микрофона в файл
        FileLogger.saveLog("WRITE_SOUND", soundArray.toString()
            .replace("[", ""),
            null)
        return false
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.i(TAG, "$TAG onBind")
        return MedicalVoiceBinder(_audioRecordingEventFlow.asSharedFlow())
    }

    override fun onDestroy() {
        Log.i(TAG, "$TAG onDestroy")

        launch(coroutineContext) {
            audioRecorderInteractor.stopRecording()
        }
        stopService()
        super.onDestroy()
    }

    private fun stopService() {
        stopForeground()
        stopSelf()
    }

    private companion object {
        const val TAG = "MedicalVoiceService"
    }
}