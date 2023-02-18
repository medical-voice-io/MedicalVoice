package io.medicalvoice.medicalvoiceservice.services

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import io.medicalvoice.medicalvoiceservice.domain.NotificationData
import io.medicalvoice.medicalvoiceservice.services.extensions.createNotificationChannel
import io.medicalvoice.medicalvoiceservice.services.extensions.startForeground
import io.medicalvoice.medicalvoiceservice.services.extensions.stopForeground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * Базовый класс сервиса, который показывает Notification
 */
abstract class BaseNotificationService : Service(), CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO

    abstract val notificationData: NotificationData
    abstract val appPackageName: String

    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        notificationManager.createNotificationChannel(
            channelId = notificationData.channelId,
            channelName = notificationData.channelName
        )
        startForeground(
            notificationManager = notificationManager,
            notificationData = notificationData,
            appPackageName = appPackageName
        )
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        stopForeground(notificationManager)
        super.onDestroy()
    }
}