package io.medicalvoice.medicalvoiceservice.domain

import java.io.Serializable

/**
 * Конфигурация канала (стерео/моно)
 */
@JvmInline
value class ChannelConfig(
    val value: Int
) : Serializable