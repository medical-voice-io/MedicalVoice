package io.medicalvoice.medicalvoiceservice.services

import android.content.Intent
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import io.medicalvoice.medicalvoiceservice.R
import io.medicalvoice.medicalvoiceservice.domain.NotificationData
import io.medicalvoice.medicalvoiceservice.logger.FileLogger
import io.medicalvoice.medicalvoiceservice.services.binders.MedicalVoiceBinder
import io.medicalvoice.medicalvoiceservice.services.events.Event
import io.medicalvoice.medicalvoiceservice.services.events.StopRecordingEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Сервис для записи аудио из микрофона в фоновом режиме */
@AndroidEntryPoint
class VoiceService : BaseNotificationService() {

    @Inject
    lateinit var audioRecorderInteractor: AudioRecorderInteractor

    override val appPackageName: String = APP_PACKAGE_NAME
    override val notificationData: NotificationData by lazy {
        NotificationData(
            channelId = VOICE_NOTIFICATION_CHANNEL_ID,
            channelName = VOICE_NOTIFICATION_CHANNEL_NAME,
            title = resources.getString(R.string.notification_title),
            text = resources.getString(R.string.notification_text),
            smallIconRes = R.drawable.ic_mic
        )
    }

    private val _audioRecordingEventFlow = MutableSharedFlow<Event>()

    // Массив куда будем собирать записанные с микрофона данные
    // TODO: не хранить данные в массиве, а записывать сразу
    private val soundArray: ArrayList<Short> = arrayListOf()

    override fun onCreate() {
        super.onCreate()
        launch {
            audioRecorderInteractor.audioBufferFlow
                .shareIn(this, started = SharingStarted.Eagerly, replay = 1)
                // При каждом полученном значении кладем в массив
                .collect { buffer ->
                    Log.i(AudioRecorder.TAG, buffer.map { it.toString() }.toString())
                    // TODO: записывать сразу в файл
                    soundArray.addAll(buffer.toList())
                }
        }
        launch {
            audioRecorderInteractor.audioRecordingEventFlow
                .shareIn(this, started = SharingStarted.Eagerly, replay = 1)
                .collect { event ->
                    if (event is StopRecordingEvent) stopSelf()
                    _audioRecordingEventFlow.emit(event)
                }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Log.i(TAG, "$TAG onStartCommand")

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
        super.onDestroy()
    }

    private companion object {
        const val TAG = "MedicalVoiceService"
        const val VOICE_NOTIFICATION_CHANNEL_ID = "VOICE_NOTIFICATION_CHANNEL_ID"
        const val VOICE_NOTIFICATION_CHANNEL_NAME = "Foreground MedicalVoice Service"
        const val APP_PACKAGE_NAME = "io.medicalvoice.android.MainActivity"
    }
}