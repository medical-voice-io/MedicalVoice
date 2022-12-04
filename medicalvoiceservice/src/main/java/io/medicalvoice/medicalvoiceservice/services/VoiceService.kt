package io.medicalvoice.medicalvoiceservice.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import io.medicalvoice.medicalvoiceservice.services.binders.MedicalVoiceBinder
import io.medicalvoice.medicalvoiceservice.services.events.Event
import io.medicalvoice.medicalvoiceservice.services.events.StopRecordingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class VoiceService(
    private val audioRecorderInteractor: AudioRecorderInteractor = AudioRecorderInteractor(
        audioRecorderRepository = AudioRecorderRepository()
    )
) : Service(), CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO

    private val _audioRecordingEventFlow = MutableSharedFlow<Event>()

    init {
        launch {
            audioRecorderInteractor.audioBufferFlow
                .shareIn(this, started = SharingStarted.Eagerly, replay = 1)
                .collect { buffer ->
                    Log.i(AudioRecorder.TAG, buffer.map { it.toString() }.toString())
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

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "$TAG onStartCommand")

        createNotificationChannel()

        val notificationIntent = Intent(
            this,
            Class.forName(APP_PACKAGE_NAME)
        )
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        val notification = NotificationCompat.Builder(this, VOICE_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Запись звука в приложении MedicalVoice")
            .setContentText("Текст уведомления")
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)

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
        return false
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.i(TAG, "$TAG onBind")
        return MedicalVoiceBinder(_audioRecordingEventFlow.asSharedFlow())
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "$TAG onDestroy")
        launch(coroutineContext) {
            audioRecorderInteractor.stopRecording()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                VOICE_NOTIFICATION_CHANNEL_ID,
                "Foreground MedicalVoiceService Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            // val serviceChannel = NotificationChannelCompat.Builder(
            //     VOICE_NOTIFICATION_CHANNEL_ID,
            //     NotificationManagerCompat.IMPORTANCE_DEFAULT
            // ).build()
            getSystemService<NotificationManager>()?.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val TAG = "MedicalVoiceService"
        private val APP_PACKAGE_NAME = "io.medicalvoice.android.MainActivity"
        private const val VOICE_NOTIFICATION_CHANNEL_ID = "VOICE_NOTIFICATION_CHANNEL_ID"

        fun startService(context: Context) {
            val intent = Intent(context, VoiceService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, VoiceService::class.java)
            context.stopService(intent)
        }

        fun bindService(context: Context, connection: ServiceConnection) {
            val intent = Intent(context, VoiceService::class.java)
            context.bindService(
                intent,
                connection,
                0 // TODO: Context.BIND_AUTO_START
            )
        }

        fun unbindService(context: Context, connection: ServiceConnection) {
            context.unbindService(connection)
        }
    }
}