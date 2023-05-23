package io.medicalvoice.medicalvoiceservice.services.binders

import android.os.Binder
import io.medicalvoice.medicalvoiceservice.services.events.Event
import kotlinx.coroutines.flow.SharedFlow

/**
 * Binder для получения данных из сервиса
 *
 * @property audioRecordingFlow флоу событий старта/остановки записи микрофона
 */
class MedicalVoiceBinder(
    val audioBufferFlow: SharedFlow<Array<DoubleArray>>,
    val audioRecordingFlow: SharedFlow<Event>
) : Binder()