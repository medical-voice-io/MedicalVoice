package io.medicalvoice.medicalvoiceservice.services.binders

import android.os.Binder
import io.medicalvoice.medicalvoiceservice.services.events.Event
import io.shiryaev.method.Frame
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Binder для получения данных из сервиса
 *
 * @property audioRecordingFlow флоу событий старта/остановки записи микрофона
 */
class MedicalVoiceBinder(
    val audioRecordingFlow: StateFlow<Event>,
    val audioFramesFlow: SharedFlow<List<Frame>>
) : Binder() {

    fun getService(): MedicalVoiceBinder = this
}