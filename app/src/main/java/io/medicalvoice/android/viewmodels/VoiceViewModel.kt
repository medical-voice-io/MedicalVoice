package io.medicalvoice.android.viewmodels

import android.app.Application
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.medicalvoice.medicalvoiceservice.domain.AudioFormat
import io.medicalvoice.medicalvoiceservice.domain.AudioRecorderConfig
import io.medicalvoice.medicalvoiceservice.domain.ChannelConfig
import io.medicalvoice.medicalvoiceservice.domain.SampleRate
import io.medicalvoice.medicalvoiceservice.services.VoiceService
import io.medicalvoice.medicalvoiceservice.services.binders.MedicalVoiceBinder
import io.medicalvoice.medicalvoiceservice.services.events.StartRecordingEvent
import io.medicalvoice.medicalvoiceservice.services.events.StopRecordingEvent
import io.medicalvoice.medicalvoiceservice.services.extensions.bindService
import io.medicalvoice.medicalvoiceservice.services.extensions.startService
import io.medicalvoice.medicalvoiceservice.services.extensions.stopService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** ViewModel экрана управления сервисом */
@HiltViewModel
class VoiceViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application), CoroutineScope by MainScope() {

    private val _isServiceRunning = MutableLiveData(false)
    val isServiceRunning: LiveData<Boolean> = _isServiceRunning

    private val serviceConnection: ServiceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
                _isServiceRunning.value = true

                Log.i(this@VoiceViewModel.javaClass.simpleName, "onServiceConnected")

                val medicalVoiceService = binder as MedicalVoiceBinder
                launch(coroutineContext) {
                    medicalVoiceService.getService().audioRecordingFlow
                        .shareIn(this, started = SharingStarted.Eagerly, replay = 1)
                        .collect { event ->
                            when (event) {
                                is StartRecordingEvent -> _isServiceRunning.value = true
                                is StopRecordingEvent -> _isServiceRunning.value = false
                            }
                        }
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                Log.i(this@VoiceViewModel.javaClass.simpleName, "onServiceDisconnected")
                _isServiceRunning.value = false
            }
        }
    }

    fun startService(
        sampleRate: Int,
        encoding: Int,
        channelFormat: Int
    ) = with(getApplication<Application>().applicationContext) {
        val audioConfig = AudioRecorderConfig(
            sampleRate = SampleRate(value = sampleRate),
            audioFormat = AudioFormat(value = encoding),
            channelConfig = ChannelConfig(value = channelFormat)
        )
        val bundleData = bundleOf(VoiceService.CONFIG_KEY to audioConfig)
        startService<VoiceService>(bundle = bundleData)
    }

    fun stopService() = with(getApplication<Application>().applicationContext) {
        stopService<VoiceService>()
    }

    fun bindService() = with(getApplication<Application>().applicationContext) {
        bindService<VoiceService>(serviceConnection)
    }

    fun unbindService() = with(getApplication<Application>().applicationContext) {
        if (_isServiceRunning.value != true) return
        unbindService(serviceConnection)
        _isServiceRunning.value = false
    }
}