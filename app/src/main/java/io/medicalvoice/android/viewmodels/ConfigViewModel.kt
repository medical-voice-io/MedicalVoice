package io.medicalvoice.android.viewmodels

import android.app.Application
import android.media.AudioFormat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.medicalvoice.android.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

/**
 * ViewModel для управления конфигурацией AudioRecord
 */
class ConfigViewModel(
    application: Application
) : AndroidViewModel(application), CoroutineScope by MainScope() {

    private val _sampleRates = mapOf(
        44100 to R.string.sample_rate_44100,
        22050 to R.string.sample_rate_22050,
        16000 to R.string.sample_rate_16000,
        11025 to R.string.sample_rate_11025,
        8000 to R.string.sample_rate_8000
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
        AudioFormat.CHANNEL_IN_STEREO to R.string.channel_stereo,
        AudioFormat.CHANNEL_IN_MONO to R.string.channel_mono
    )
    val channels = _channels

    private val _selectedSampleRate = MutableLiveData(_sampleRates.keys.first())
    val selectedSampleRate: LiveData<Int> = _selectedSampleRate

    private val _selectedEncoding = MutableLiveData(_quantizationBitRates.keys.first())
    val selectedEncoding: LiveData<Int> = _selectedEncoding

    private val _selectedChannelFormat = MutableLiveData(_channels.keys.first())
    val selectedChannelFormat: LiveData<Int> = _selectedChannelFormat

    fun selectSampleRate(sampleRate: Int) {
        _selectedSampleRate.value = sampleRate
    }

    fun selectEncoding(encoding: Int) {
        _selectedEncoding.value = encoding
    }

    fun selectChannelFormat(channelFormat: Int) {
        _selectedChannelFormat.value = channelFormat
    }
}