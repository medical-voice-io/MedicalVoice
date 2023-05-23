package io.medicalvoice.medicalvoiceservice.services.extensions

// Максимальное значение амплитуды. Необходимо для нормализаци:
// чтобы значения находились от -1 до 1
private const val maxAmplitude = Short.MAX_VALUE + 1.0f

/**
 * Нормализует значения амплитуды
 */
internal fun ShortArray.normalize(): List<Float> {
    return map { amplitude -> amplitude / maxAmplitude }
}