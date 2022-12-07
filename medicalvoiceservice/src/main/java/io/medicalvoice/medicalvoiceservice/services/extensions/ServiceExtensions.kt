package io.medicalvoice.medicalvoiceservice.services.extensions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService

private const val VOICE_NOTIFICATION_CHANNEL_ID = "VOICE_NOTIFICATION_CHANNEL_ID"
private const val APP_PACKAGE_NAME = "io.medicalvoice.android.MainActivity"

/** Создает канал для уведомления */
fun Service.createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val serviceChannel = NotificationChannel(
            VOICE_NOTIFICATION_CHANNEL_ID,
            "Foreground MedicalVoiceService Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        getSystemService<NotificationManager>()?.createNotificationChannel(serviceChannel)
    }
}

/** Запускает уведомление и привязывает сервис к foreground */
fun Service.startForeground() {
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
}