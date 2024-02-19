package io.shiryaev

import android.util.Log
import io.shiryaev.method.Frame
import io.shiryaev.usecase.GetPowerUseCase
import io.shiryaev.usecase.KmeansMethodUseCase
import io.shiryaev.usecase.NeymanPearsonUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PreprocessingUseCase @Inject constructor(
    private val getPowerUseCase: GetPowerUseCase,
    private val nMeansMethodUseCase: KmeansMethodUseCase,
    private val neymanPearsonUseCase: NeymanPearsonUseCase,
) {

    suspend operator fun invoke(
        countNumberForFft: Int,
        amplitudes: ShortArray,
        threshold: Double,
    ): List<Frame> = withContext(Dispatchers.Default) {
        val frames = getPowerUseCase(
            countNumberForFft = countNumberForFft,
            amplitude = amplitudes.normalize()
        ).mapIndexed { index, power ->
            Frame(
                id = index,
                power = power
            )
        }
        val maxPower = frames.maxBy { frame -> frame.power }.power

        val totalEnergy = frames.sumOf { it.power }
        val averageEnergy = totalEnergy / frames.size
        if (averageEnergy > threshold) {
            Log.i("IS_NOISE", "Есть что-то кроме шума")
            mainPreprocessing(
                frames = frames,
                maxPower = maxPower
            )
        } else {
            Log.i("IS_NOISE", "Шум: $averageEnergy")
            frames.map { it.copy(isNoise = true) }
        }
    }

    private suspend fun mainPreprocessing(
        frames: List<Frame>,
        maxPower: Double
    ): List<Frame> {
        val clusters = nMeansMethodUseCase(
            frames = frames,
            k = 3
        )

        val noiseCluster = clusters.minByOrNull { cluster ->
            cluster.centroid
        } ?: return emptyList()

        /*
        Нужно удалить последний кластер с шумом.
        С шумом кластер имеет наименьший центроид.
        Для этого сначала сортируем кластеры по центроидам.
        Удаляем последний кластер (с наименьшим центроидом)
         */
        val clustersWithoutNoise = clusters.sortedBy { cluster ->
            cluster.centroid
        }.dropLast(1)

        val framesWithAnyNoise = neymanPearsonUseCase(
            frames = noiseCluster.points,
            b = 1.7
        )

        return buildList {
            addAll(clustersWithoutNoise.flatMap { cluster -> cluster.points })
            addAll(framesWithAnyNoise)
        }
            .sortedBy { frame -> frame.id }
            .map { frame ->
                frame.copy(
                    power = frame.power
                )
            }
    }

    private fun ShortArray.normalize(): List<Double> = map {
        it / Short.MAX_VALUE.toDouble()
    }
}