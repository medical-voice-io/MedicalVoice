package io.shiryaev.usecase

import io.shiryaev.algoritms.Algoritms
import io.shiryaev.data.Complex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetPowerUseCase @Inject constructor(
    private val algoritms: Algoritms,
) {
    suspend operator fun invoke(
        countNumberForFft: Int,
        amplitude: List<Double>,
    ): List<Double> = withContext(Dispatchers.Default) {
        /* Кол-во итераций для исходного кол-ва амплитуд */
        val K = algoritms.numberIterationsForAmplitudes(
            totalSignalNumber = amplitude.size,
            countNumbersForFft = countNumberForFft
        )
        /* Сигнал в виде ряда Фурье */
        val Xk = algoritms.directFourierTransformApache(
            amplitudes = amplitude,
            countNumbersForFft = countNumberForFft,
            numberIterationsForAmplitudes = K
        )
        /* Спектр амплитуд */
        // val Vk = getAmplitudeSpectrum(Xk)
        val Vk = Xk.map { spectrum ->
            spectrum.map { complex -> complex.abs() }
        }
        /* Мощность */
        getPower(Vk)
    }

    /**
     * Получение спектра амплитуд для всех окон
     * @param complexSpectrum спектр в комплексном виде для всех окон
     */
    private fun getAmplitudeSpectrum(
        complexSpectrum: List<List<Complex>>
    ): List<List<Double>> = complexSpectrum.map { spectrumInFrame ->
        algoritms.amplitudeSpectrum(spectrumInFrame)
    }

    /**
     * Получение мощности для всех окон
     * @param amplitudeSpectrum спектр амплитуд для всех окон
     */
    private fun getPower(
        amplitudeSpectrum: List<List<Double>>
    ): List<Double> = amplitudeSpectrum.map { spectrumInFrame ->
        algoritms.powerInFrame(spectrumInFrame)
    }
}