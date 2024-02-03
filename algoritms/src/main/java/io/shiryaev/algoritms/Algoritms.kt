package io.shiryaev.algoritms

import io.shiryaev.data.Complex
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt
import org.apache.commons.math3.complex.Complex as ApacheComplex

class Algoritms @Inject constructor() {

    /**
     * Количество отсчетов
     * @param frequency частота дискретизации
     * @param stationarityPeriod период стационарности
     * @return количество отсчетов на длительности сигнала [stationarityPeriod]
     * */
    fun getCountNumbers(
        frequency: Int,
        stationarityPeriod: Double = 0.02,
    ) = stationarityPeriod * frequency

    /**
     * Ближайшее число в степени 2
     * @param countNumbers количество отсчетов
     * @return ближайшее число в степени 2 для количества отсчетов [countNumbers]
     */
    fun nearestNumberInPowerOf2(countNumbers: Double): Int = log2(countNumbers).toInt()

    /**
     * Ближайшее число в степени 2
     * @param frequency частота дискретизации
     * @param stationarityPeriod период стационарности
     * @return ближайшее число в степени 2 для количества отсчетов [countNumbers]
     */
    fun nearestNumberInPowerOf2(
        frequency: Int,
        stationarityPeriod: Double = 0.02,
    ): Int = nearestNumberInPowerOf2(
        countNumbers = getCountNumbers(frequency, stationarityPeriod)
    )

    /**
     * Количество отсчетов для быстрого преобразования Фурье
     * @param nearestNumberInPowerOf2 ближайшее число в степени 2
     * @return Размер окна, выраженный в количестве отсчетов
     */
    fun countNumbersForFft(
        nearestNumberInPowerOf2: Int,
    ): Int = 2.0.pow(nearestNumberInPowerOf2).toInt()

    /**
     * Вычисление количества отсчетов для преобразования Фурье
     * @param totalSignalNumber общее количество отсчетов сигнала
     * @param countNumbersForFft количество отсчетов для быстрого преобразования Фурье
     * @return Количество итераций для преобразования Фурье
     */
    fun numberIterationsForAmplitudes(
        totalSignalNumber: Int,
        countNumbersForFft: Int,
    ): Int = ceil(totalSignalNumber.toDouble() / countNumbersForFft.toDouble()).roundToInt()

    /**
     * Прямое преобразование Фурье в одном окне
     * @param amplitudes амплитуды
     * @param countNumbersForFft количество отсчетов для быстрого преобразования Фурье
     * @param numberIterationsForAmplitudes количество итераций для амплитуд
     */
    fun directFourierTransform(
        amplitudes: List<Double>,
        countNumbersForFft: Int,
        numberIterationsForAmplitudes: Int,
    ): List<List<Complex>> = buildList {
        repeat(numberIterationsForAmplitudes) { index ->
            val startIndex = index * countNumbersForFft
            val endIndex = startIndex + countNumbersForFft
            val frame = amplitudes.subList(
                startIndex,
                endIndex
            )
            val spectrum = fft(frame)
            add(spectrum)
        }
    }

    fun directFourierTransformApache(
        amplitudes: List<Double>,
        countNumbersForFft: Int,
        numberIterationsForAmplitudes: Int,
    ): List<List<ApacheComplex>> {
        val fft = FastFourierTransformer(
            DftNormalization.STANDARD
        )
        return buildList {
            repeat(numberIterationsForAmplitudes) { index ->
                val startIndex = index * countNumbersForFft
                val endIndex = startIndex + countNumbersForFft
                val frame = amplitudes.subList(
                    startIndex,
                    endIndex
                ).toDoubleArray()
                val spectrum = fft.transform(
                    frame,
                    TransformType.FORWARD
                ).toList()
                add(spectrum)
            }
        }
    }

    /**
     * Вычисление спектра амплитуд
     * @param complexSpectrum спектр в комплексном виде
     */
    fun amplitudeSpectrum(
        complexSpectrum: List<Complex>
    ): List<Double> = complexSpectrum.map { it.magnitude() }

    /**
     * Вычисление мощности сигнала
     * @param amplitudeSpectrum спектр амплитуд
     */
    fun powerInFrame(
        amplitudeSpectrum: List<Double>
    ): Double = amplitudeSpectrum.sumOf { it.pow(2) } / 2

    /**
     * Фурье преобразование
     * @param amplitudes амплитуды
     */
    private fun fft(amplitudes: List<Double>): List<Complex> {
        val N = amplitudes.size
        if (N == 1) return amplitudes.map { Complex(it, 0.0) }

        val even = fft(amplitudes.filterIndexed { index, _ -> index % 2 == 0 })
        val odd = fft(amplitudes.filterIndexed { index, _ -> index % 2 == 1 })

        return buildList {
            repeat(N / 2) { k ->
                val t = Complex.polar(1.0, -2.0 * Math.PI * k / N) * odd[k]
                add(even[k] + t)
                add(even[k] - t)
            }
        }
    }
}