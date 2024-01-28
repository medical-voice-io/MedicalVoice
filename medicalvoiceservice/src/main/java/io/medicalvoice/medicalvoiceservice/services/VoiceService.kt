package io.medicalvoice.medicalvoiceservice.services

import android.content.Intent
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import io.medicalvoice.medicalvoiceservice.R
import io.medicalvoice.medicalvoiceservice.domain.AudioRecorderConfig
import io.medicalvoice.medicalvoiceservice.domain.NotificationData
import io.medicalvoice.medicalvoiceservice.services.binders.MedicalVoiceBinder
import io.medicalvoice.medicalvoiceservice.services.events.Event
import io.medicalvoice.medicalvoiceservice.services.events.StopRecordingEvent
import io.medicalvoice.medicalvoiceservice.services.extensions.getSerializable
import io.shiryaev.method.Frame
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _audioRecordingEventFlow = MutableStateFlow<Event>(StopRecordingEvent)
    private val _audioFramesFlow = MutableSharedFlow<List<Frame>>()

    override fun onCreate() {
        super.onCreate()
        launch {
            audioRecorderInteractor.audioBufferFlow
                .collect { buffer ->
                    _audioFramesFlow.emit(buffer)
                }
        }
        launch {
            audioRecorderInteractor.audioRecordingEventFlow
                .collect { event ->
                    _audioRecordingEventFlow.emit(event)
                    if (event is StopRecordingEvent) stopSelf()
                }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Log.i(TAG, "$TAG onStartCommand")

        val audioRecorderConfig = intent.getSerializable<AudioRecorderConfig>(CONFIG_KEY)

        launch(coroutineContext) {
            audioRecorderInteractor.startRecording(audioRecorderConfig)
            // TODO: зачем?
            // stopSelf()
        }
        return START_STICKY
    }

    override fun stopService(name: Intent?): Boolean {
        Log.i(TAG, "$TAG stopService")
        audioRecorderInteractor.stopRecording()
        return super.stopService(name)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "$TAG onUnbind")
        return false
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.i(TAG, "$TAG onBind")
        return MedicalVoiceBinder(
            audioRecordingFlow = _audioRecordingEventFlow.asStateFlow(),
            audioFramesFlow = _audioFramesFlow.asSharedFlow()
        )
    }

    override fun onDestroy() {
        Log.i(TAG, "$TAG onDestroy")

        super.onDestroy()
        audioRecorderInteractor.stopRecording()
    }

    companion object {
        const val CONFIG_KEY = "AudioRecorderConfig"

        private const val TAG = "MedicalVoiceService"
        private const val VOICE_NOTIFICATION_CHANNEL_ID = "VOICE_NOTIFICATION_CHANNEL_ID"
        private const val VOICE_NOTIFICATION_CHANNEL_NAME = "Foreground MedicalVoice Service"
        private const val APP_PACKAGE_NAME = "io.medicalvoice.android.MainActivity"
    }
}