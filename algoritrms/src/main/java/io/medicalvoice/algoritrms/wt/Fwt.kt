package io.medicalvoice.algoritrms.wt

import javax.inject.Inject
import kotlin.math.abs

/**
 * Быстрое вейвлет-преобразование
 *
 * @property buffer Буффер, кадр. Содержит набор амплитуд.
 * Длина набора должна быть в степени 2 (2ˆN)
 */
class Fwt @Inject constructor() {

    // /** Масштаб, обычно отличающийся в 2 раза: 2ˆm */
    // private val a: Int

    // /** Сдвиг, обычно кратный некоторому числу l: a*l */
    // private val t: Int

    /**
     * Прямое преобразование
     * @param buffer Буффер, кадр. Содержит набор амплитуд.
     * Длина набора должна быть в степени 2 (2ˆN)
     */
    fun directTransform(buffer: List<Float>): List<Float> {
        if (buffer.size == 1) return buffer

        val retVal = mutableListOf<Float>()
        val tmpArr = mutableListOf<Float>()

        for (i in 0 until buffer.size - 1 step 2) {
            // Простое вейвлет-преобразование, используется для одномерного массива амплитуд.
            // Полусумма и полуразность являются простейшими фильтрами для вейвлета Haar.
            // Для других вейвлет нужно использовать другие фильтры верхних и нижних частот
            retVal.add((buffer[i] - buffer[i + 1]) / 2) // Дает детализацию
            tmpArr.add((buffer[i] + buffer[i + 1]) / 2) // Дает аппроксимацию
        }

        retVal.addAll(directTransform(tmpArr))

        return retVal
    }

    /**
     * Ступенчатый вейвлет
     */
    private fun weveletHaar(t: Float): Int = when {
        0 <= t && t < 0.5 -> 1
        0.5 <= t && t < 1 -> -1
        else -> 0
    }

    private fun wabeletFhat(t: Float): Int {
        val absT = abs(t)
        return when {
            t <= 1 / 3 -> 1
            1 / 3 < t && t <= 1 -> -1 / 2
            else -> 0
        }
    }

    // private fun ksi(m: Int, l: Int) {
    //     return (2**(-m/2))*ksi_isxodnyi(2ˆ(-mt)-l)
    // }
}