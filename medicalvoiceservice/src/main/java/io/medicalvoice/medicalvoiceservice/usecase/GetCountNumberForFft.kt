package io.medicalvoice.medicalvoiceservice.usecase

import io.shiryaev.algoritms.Algoritms
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetCountNumberForFft @Inject constructor(
    private val algoritms: Algoritms,
) {

    suspend operator fun invoke(
        frequency: Int,
        stationarityPeriod: Double = 0.2
    ): Int = withContext(Dispatchers.Default) {
        /* Ближайшее число степени двойки */
        val s = algoritms.nearestNumberInPowerOf2(
            frequency = frequency,
            stationarityPeriod = stationarityPeriod
        )
        /* Кол-во отсчетов для БПФ */
        algoritms.countNumbersForFft(s)
    }
}