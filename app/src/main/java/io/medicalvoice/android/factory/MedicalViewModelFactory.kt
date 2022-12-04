package io.medicalvoice.android.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.medicalvoice.android.VoiceViewModel

class MedicalViewModelFactory(
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VoiceViewModel(application) as T
    }
}