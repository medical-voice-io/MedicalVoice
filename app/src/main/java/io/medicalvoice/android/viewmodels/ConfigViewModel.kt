package io.medicalvoice.android.viewmodels

import android.app.Application
import android.media.AudioFormat
import androidx.lifecycle.AndroidViewModel
import io.medicalvoice.android.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel для управления конфигурацией AudioRecord
 */
class ConfigViewModel(
    application: Application
) : AndroidViewModel(application), CoroutineScope by MainScope() {

    private val _sampleRates = mapOf(
        8000 to R.string.sample_rate_8000,
        11025 to R.string.sample_rate_11025,
        16000 to R.string.sample_rate_16000,
        22050 to R.string.sample_rate_22050,
        44100 to R.string.sample_rate_44100,
    )
    val sampleRate = _sampleRates

    private val _quantizationBitRates = mapOf(
        AudioFormat.ENCODING_PCM_16BIT to R.string.encoding_pcm_16_bit,
        AudioFormat.ENCODING_PCM_8BIT to R.string.encoding_pcm_8_bit,
        AudioFormat.ENCODING_PCM_FLOAT to R.string.encoding_pcm_float,
        AudioFormat.ENCODING_DEFAULT to R.string.encoding_default
    )
    val quantizationBitRates = _quantizationBitRates

    private val _channels = mapOf(
        AudioFormat.CHANNEL_IN_MONO to R.string.channel_mono,
        // AudioFormat.CHANNEL_IN_STEREO to R.string.channel_stereo
    )
    val channels = _channels

    private val _selectedSampleRate = MutableStateFlow(_sampleRates.keys.first())
    val selectedSampleRate: StateFlow<Int> = _selectedSampleRate

    private val _selectedEncoding = MutableStateFlow(_quantizationBitRates.keys.first())
    val selectedEncoding: StateFlow<Int> = _selectedEncoding

    private val _selectedChannelFormat = MutableStateFlow(_channels.keys.first())
    val selectedChannelFormat: StateFlow<Int> = _selectedChannelFormat

    private val _threshold = MutableStateFlow("0.0")
    val threshold: StateFlow<String> = _threshold.asStateFlow()

    fun selectSampleRate(sampleRate: Int) {
        _selectedSampleRate.value = sampleRate
    }

    fun selectEncoding(encoding: Int) {
        _selectedEncoding.value = encoding
    }

    fun selectChannelFormat(channelFormat: Int) {
        _selectedChannelFormat.value = channelFormat
    }

    fun onThresholdTextChange(thresholdText: String) {
        _threshold.value = thresholdText
    }
}