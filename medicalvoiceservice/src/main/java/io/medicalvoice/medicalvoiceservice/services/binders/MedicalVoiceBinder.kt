package io.medicalvoice.medicalvoiceservice.services.binders

import android.os.Binder
import io.medicalvoice.medicalvoiceservice.services.events.Event
import kotlinx.coroutines.flow.StateFlow

/**
 * Binder для получения данных из сервиса
 *
 * @property audioRecordingFlow флоу событий старта/остановки записи микрофона
 */
class MedicalVoiceBinder(
    val audioRecordingFlow: StateFlow<Event>
) : Binder() {

    fun getService(): MedicalVoiceBinder = this
}