package io.shiryaev.algoritms

import io.shiryaev.data.Complex
import kotlin.math.log2
import kotlin.math.pow

class Algoritms {

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
    fun nearestNumberInPowerOf2(countNumbers: Double) = log2(countNumbers)

    /**
     * Ближайшее число в степени 2
     * @param frequency частота дискретизации
     * @param stationarityPeriod период стационарности
     * @return ближайшее число в степени 2 для количества отсчетов [countNumbers]
     */
    fun nearestNumberInPowerOf2(
        frequency: Int,
        stationarityPeriod: Double = 0.02,
    ) = nearestNumberInPowerOf2(
        countNumbers = getCountNumbers(frequency, stationarityPeriod)
    )

    /**
     * Количество отсчетов для быстрого преобразования Фурье
     * @param nearestNumberInPowerOf2 ближайшее число в степени 2
     * @return Размер окна, выраженный в количестве отсчетов
     */
    fun countNumbersForFft(
        nearestNumberInPowerOf2: Double,
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
    ): Int = totalSignalNumber / countNumbersForFft

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
    ): List<List<Complex>> {
        val complexSpectrum = mutableListOf<List<Complex>>()
        for (i in 0 until numberIterationsForAmplitudes) {
            val startIndex = i * countNumbersForFft
            val endIndex = startIndex + countNumbersForFft
            val frame = amplitudes.subList(startIndex, endIndex)
            val spectrum = fft(frame)
            complexSpectrum.add(spectrum)
        }
        return complexSpectrum
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

        val combined = mutableListOf<Complex>()
        for (k in 0 until N / 2) {
            val t = Complex.polar(1.0, -2 * Math.PI * k / N) * odd[k]
            combined.add(even[k] + t)
            combined.add(even[k] - t)
        }
        return combined
    }
}