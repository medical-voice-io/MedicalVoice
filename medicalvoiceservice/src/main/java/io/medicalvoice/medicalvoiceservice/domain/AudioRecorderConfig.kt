package io.medicalvoice.medicalvoiceservice.domain

import java.io.Serializable

/**
 * Конфигурация для AudioRecord
 *
 * @property sampleRate частота дискретизация
 * @property audioFormat разрядность квантования
 * @property channelConfig Режим записи
 * @property threshold Коэффициент для рассчета порога
 */
class AudioRecorderConfig(
    val sampleRate: SampleRate,
    val audioFormat: AudioFormat,
    val channelConfig: ChannelConfig,
    val threshold: Double
) : Serializable