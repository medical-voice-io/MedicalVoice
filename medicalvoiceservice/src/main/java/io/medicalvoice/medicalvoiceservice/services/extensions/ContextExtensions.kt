package io.medicalvoice.medicalvoiceservice.services.extensions

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import androidx.core.os.bundleOf

/** Запуск сервиса */
inline fun <reified T : Service> Context.startService(bundle: Bundle = bundleOf()) {
    val intent = Intent(this, T::class.java).apply {
        putExtras(bundle)
    }
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
inline fun <reified T : Service> Context.bindService(connection: ServiceConnection): Boolean {
    Intent(this, T::class.java).also { intent ->
        return bindService(
            intent,
            connection,
            0 // TODO: Context.BIND_AUTO_START
        )
    }
}
