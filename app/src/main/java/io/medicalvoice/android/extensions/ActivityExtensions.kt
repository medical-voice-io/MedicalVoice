package io.medicalvoice.android.extensions

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat

// TODO: Правильно реализовать работу с пермишенами
/**
 * Запрашивает разрешение для записи аудио
 */
fun ComponentActivity.checkAudioPermission(permissions: Set<String>) {
    if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            200
        )
    }
}