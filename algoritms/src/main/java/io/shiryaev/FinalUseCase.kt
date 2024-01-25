package io.shiryaev

import io.shiryaev.method.Frame
import io.shiryaev.usecase.GetPowerUseCase
import io.shiryaev.usecase.KmeansMethodUseCase
import io.shiryaev.usecase.NeymanPearsonUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FinalUseCase @Inject constructor(
    private val getPowerUseCase: GetPowerUseCase,
    private val nMeansMethodUseCase: KmeansMethodUseCase,
    private val neymanPearsonUseCase: NeymanPearsonUseCase,
) {

    suspend operator fun invoke(
        frequency: Int,
        amplitudes: List<Double>
    ): List<Frame> = withContext(Dispatchers.Default) {
        val frames = getPowerUseCase(
            frequency = frequency,
            amplitude = amplitudes
        ).mapIndexed { index, power ->
            Frame(
                id = index,
                power = power
            )
        }

        val clusters = nMeansMethodUseCase(
            frames = frames,
            k = 3
        )

        val noiseCluster = clusters.minByOrNull { it.centroid } ?: error("Noise cluster not found")

        /*
        Нужно удалить последний кластер с шумом.
        С шумом кластер имеет наименьший центроид.
        Для этого сначала сортируем кластеры по центроидам.
        Удаляем последний кластер (с наименьшим центроидом)
         */
        val clustersWithoutNoise =
            clusters.sortedBy { cluster -> cluster.centroid }.drop(clusters.lastIndex)

        val framesWithAnyNoise = neymanPearsonUseCase(
            frames = noiseCluster.points,
            b = 3.0
        )

        val finalPowerFrames = buildList {
            addAll(clustersWithoutNoise.flatMap { cluster -> cluster.points })
            addAll(framesWithAnyNoise)
        }.sortedBy { frame -> frame.id }

        finalPowerFrames
    }
}