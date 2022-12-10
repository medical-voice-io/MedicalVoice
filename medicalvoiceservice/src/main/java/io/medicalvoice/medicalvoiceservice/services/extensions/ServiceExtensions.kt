package io.medicalvoice.medicalvoiceservice.services.extensions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import io.medicalvoice.medicalvoiceservice.domain.NotificationData

/** Создает канал для уведомления */
fun NotificationManager.createNotificationChannel(
    channelId: String,
    channelName: String
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val serviceChannel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        createNotificationChannel(serviceChannel)
    }
}

/** Запускает уведомление и привязывает сервис к foreground */
fun Service.startForeground(
    notificationManager: NotificationManager,
    notificationData: NotificationData,
    appPackageName: String
) {
    val notificationIntent = Intent(
        this,
        Class.forName(appPackageName)
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
    val notification = NotificationCompat.Builder(this, notificationData.channelId)
        .setSmallIcon(notificationData.smallIconRes)
        .setContentTitle(notificationData.title)
        .setContentText(notificationData.text)
        .setContentIntent(pendingIntent)
        .build()
    startForeground(1, notification)
    notificationManager.notify(0, notification)
}

fun Service.stopForeground(notificationManager: NotificationManager) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        stopForeground(Service.STOP_FOREGROUND_REMOVE)
    } else {
        stopForeground(false)
    }
    notificationManager.cancelAll()
}