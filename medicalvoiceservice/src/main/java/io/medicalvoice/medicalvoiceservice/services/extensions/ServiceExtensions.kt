package io.medicalvoice.medicalvoiceservice.services.extensions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import io.medicalvoice.medicalvoiceservice.R

private const val VOICE_NOTIFICATION_CHANNEL_ID = "VOICE_NOTIFICATION_CHANNEL_ID"
private const val APP_PACKAGE_NAME = "io.medicalvoice.android.MainActivity"

/** Создает канал для уведомления */
fun NotificationManager.createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val serviceChannel = NotificationChannel(
            VOICE_NOTIFICATION_CHANNEL_ID,
            "Foreground MedicalVoiceService Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        createNotificationChannel(serviceChannel)
    }
}

/** Запускает уведомление и привязывает сервис к foreground */
fun Service.startForeground(notificationManager: NotificationManager) {
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
        .setSmallIcon(R.drawable.ic_android)
        .setContentTitle("Запись звука в приложении MedicalVoice")
        .setContentText("Текст уведомления")
        .setContentIntent(pendingIntent)
        .build()
    startForeground(1, notification)
    notificationManager.notify(0, notification)
}

fun Service.startForegroundAndShowNotification() {
    val notificationManager = (getSystemService(
        Context.NOTIFICATION_SERVICE
    ) as NotificationManager).apply { createNotificationChannel() }
    startForeground(notificationManager)
}

fun Service.stopForeground() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        stopForeground(Service.STOP_FOREGROUND_DETACH)
    } else {
        stopForeground(false)
    }
}