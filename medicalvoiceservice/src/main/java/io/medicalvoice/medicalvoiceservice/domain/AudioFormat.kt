package io.medicalvoice.medicalvoiceservice.domain

import java.io.Serializable

/**
 * Формат записи аудио, разрядность квантования
 */
@JvmInline
value class AudioFormat(
    val value: Int
) : Serializable