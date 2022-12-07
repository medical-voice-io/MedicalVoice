package io.medicalvoice.medicalvoiceservice.services.extensions

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build

/** Запуск сервиса */
inline fun <reified T: Service> Context.startService() {
    val intent = Intent(this, T::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}

/** Останавливает сервис */
inline fun <reified T: Service> Context.stopService() {
    val intent = Intent(this, T::class.java)
    stopService(intent)
}

/**
 * Привязывает сервис
 *
 * @param connection ServiceConnection для передачи данных
 */
inline fun <reified T: Service> Context.bindService(connection: ServiceConnection) {
    val intent = Intent(this, T::class.java)
    bindService(
        intent,
        connection,
        0 // TODO: Context.BIND_AUTO_START
    )
}

/**
 * Отвязывает сервис
 *
 * @param connection ServiceConnection для передачи данных
 */
fun Context.unbindService(connection: ServiceConnection) {
    unbindService(connection)
}
