package io.medicalvoice.medicalvoiceservice.services.binders

import android.os.Binder
import io.medicalvoice.medicalvoiceservice.services.events.Event
import kotlinx.coroutines.flow.SharedFlow

class MedicalVoiceBinder(
    val audioRecordingFlow: SharedFlow<Event>
) : Binder() {

    fun getService(): MedicalVoiceBinder = this
}