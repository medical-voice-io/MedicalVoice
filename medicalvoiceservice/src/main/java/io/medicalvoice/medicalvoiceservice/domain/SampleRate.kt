package io.medicalvoice.medicalvoiceservice.domain

import java.io.Serializable

/**
 * Частота дискретизации
 *
 * @property value значение частоты дискретизации
 */
@JvmInline
value class SampleRate(
    val value: Int
) : Serializable