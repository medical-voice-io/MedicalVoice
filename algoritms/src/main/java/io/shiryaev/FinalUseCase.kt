package io.shiryaev

import io.shiryaev.method.Frame
import io.shiryaev.usecase.GetPowerUseCase
import io.shiryaev.usecase.KmeansMethodUseCase
import io.shiryaev.usecase.NeymanPearsonUseCase
import javax.inject.Inject

class FinalUseCase @Inject constructor(
    private val getPowerUseCase: GetPowerUseCase,
    private val nMeansMethodUseCase: KmeansMethodUseCase,
    private val neymanPearsonUseCase: NeymanPearsonUseCase,
) {

    suspend operator fun invoke(
        frequency: Int,
        amplitudes: List<Double>
    ) {
        val frames = getPowerUseCase(
            frequency = frequency,
            amplitude = amplitudes
        ).map { power -> Frame(power) }

        val clusters = nMeansMethodUseCase(
            frames = frames,
            k = 3
        )

        val noiseCluster = clusters.minByOrNull { it.centroid } ?: error("Noise cluster not found")
    }
}