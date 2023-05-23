package io.medicalvoice.algoritrms.wt

import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Непрерывное вейвлет-преобразование
 */
class Cwt @Inject constructor() {

    fun directTransform(
        signal: List<Float>,
        scale: Int = DEFAULT_SCALE
    ): Array<DoubleArray> {
        val result = Array(scale) { DoubleArray(signal.size) }

        // Цикл по шкале
        for (k in 0 until scale) {
            val fStep = 2.0 * PI / (k + 1).toDouble()

            // Цикл по времени
            for (i in signal.indices) {
                val sum = DoubleArray(2)

                // Цикл по частоте
                for (j in 0 until k + 1) {
                    val phase = j * fStep
                    val cosPhase = cos(phase)
                    val sinPhase = sin(phase)
                    val value = signal.loopAccess(i - j)
                    sum[0] += value * cosPhase
                    sum[1] += value * sinPhase
                }

                result[k][i] = sqrt(sum[0] * sum[0] + sum[1] * sum[1])
            }
        }
        return result
    }

    private fun List<Float>.loopAccess(i: Int): Float {
        val index = when {
            i < 0 -> 0
            i >= size -> size - 1
            else -> i
        }
        return this[index]
    }

    private companion object {
        const val DEFAULT_SCALE = 64
    }
}