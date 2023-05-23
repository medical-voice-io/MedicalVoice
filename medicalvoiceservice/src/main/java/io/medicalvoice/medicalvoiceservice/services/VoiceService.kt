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
import io.medicalvoice.medicalvoiceservice.services.usecases.record.AudioRecorder
import io.medicalvoice.medicalvoiceservice.services.usecases.record.AudioRecorderUseCase
import io.medicalvoice.medicalvoiceservice.services.usecases.transform.TransformAlgorithmUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Сервис для записи аудио из микрофона в фоновом режиме */
@AndroidEntryPoint
class VoiceService : BaseNotificationService() {

    @Inject
    lateinit var audioRecorderUseCase: AudioRecorderUseCase

    @Inject
    lateinit var transformAlgorithmUseCase: TransformAlgorithmUseCase

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

    private val _audioBufferFlow = MutableSharedFlow<Array<DoubleArray>>()
    private val _audioRecordingEventFlow = MutableSharedFlow<Event>()

    override fun onCreate() {
        super.onCreate()
        launch {
            transformAlgorithmUseCase
                .coefficients
                .shareIn(this, started = SharingStarted.Eagerly, replay = 1)
                .collect { coefficients ->
                    _audioBufferFlow.emit(coefficients)
                }
        }
        launch {
            audioRecorderUseCase.audioBufferFlow
                .shareIn(this, started = SharingStarted.Eagerly, replay = 1)
                .collect { buffer ->
                    Log.i(AudioRecorder.TAG, buffer.map { it.toString() }.toString())
                    Log.i(AudioRecorder.TAG, "Buffer size: ${buffer.size}")
                    transformAlgorithmUseCase.getCoefficients(buffer)
                }
        }
        launch {
            audioRecorderUseCase.audioRecordingEventFlow
                .shareIn(this, started = SharingStarted.Eagerly, replay = 1)
                .collect { event ->
                    if (event is StopRecordingEvent) stopSelf()
                    _audioRecordingEventFlow.emit(event)
                }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Log.i(TAG, "$TAG onStartCommand")

        val audioRecorderConfig = intent.getSerializable<AudioRecorderConfig>(CONFIG_KEY)

        launch(coroutineContext) {
            audioRecorderUseCase.startRecording(audioRecorderConfig)
            stopSelf()
        }
        return START_STICKY
    }

    override fun stopService(name: Intent?): Boolean {
        Log.i(TAG, "$TAG stopService")
        launch(coroutineContext) {
            audioRecorderUseCase.stopRecording()
        }
        return super.stopService(name)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "$TAG onUnbind")
        return false
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.i(TAG, "$TAG onBind")
        return MedicalVoiceBinder(
            audioBufferFlow = _audioBufferFlow,
            audioRecordingFlow = _audioRecordingEventFlow.asSharedFlow()
        )
    }

    override fun onDestroy() {
        Log.i(TAG, "$TAG onDestroy")

        launch(coroutineContext) {
            audioRecorderUseCase.stopRecording()
        }
        super.onDestroy()
    }

    companion object {
        const val CONFIG_KEY = "AudioRecorderConfig"

        private const val TAG = "MedicalVoiceService"
        private const val VOICE_NOTIFICATION_CHANNEL_ID = "VOICE_NOTIFICATION_CHANNEL_ID"
        private const val VOICE_NOTIFICATION_CHANNEL_NAME = "Foreground MedicalVoice Service"
        private const val APP_PACKAGE_NAME = "io.medicalvoice.android.MainActivity"
    }
}