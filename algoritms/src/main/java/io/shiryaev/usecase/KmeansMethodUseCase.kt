package io.shiryaev.usecase

import io.shiryaev.method.Cluster
import io.shiryaev.method.Frame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Метод k-средних
 */
class KmeansMethodUseCase @Inject constructor() {

    /**
     * @param frames окна
     * @param k количество кластеров
     */
    suspend operator fun invoke(
        frames: List<Frame>,
        k: Int,
        maxIteration: Int = 100
    ): List<Cluster> = withContext(Dispatchers.Default) {
        // Инициализация центроидов случайными кадрами из входных данных
        val centroids = frames.shuffled().take(k).map { frame ->
            frame.power
        }.toMutableList()

        var clusters = emptyList<Cluster>()
        var currentIteration = 0

        while (currentIteration < maxIteration) {
            clusters = frames
                .map { frame ->
                    val closestCentroid = centroids.minByOrNull { centroid ->
                        euclideanDistance(centroid, frame.power)
                    } ?: error("Centroid not found")

                    Cluster(
                        closestCentroid,
                        listOf(frame)
                    )
                }
                .groupBy { cluster -> cluster.centroid }
                .map { (centroid, clusterFrames) ->
                    Cluster(centroid, clusterFrames.flatMap { cluster -> cluster.points })
                }

            // Пересчет центроидов
            val newCentroids = clusters.map { cluster ->
                cluster.points.map { frame ->
                    frame.power
                }.average()
            }

            // Проверка на сходимость
            if (newCentroids == centroids) {
                break
            }

            centroids.clear()
            centroids.addAll(newCentroids)
            currentIteration++
        }

        clusters
    }

    /**
     * Евклидово расстояние
     * @param firstPoint
     * @param secondPoint
     */
    private fun euclideanDistance(
        firstPoint: Double,
        secondPoint: Double
    ): Double = sqrt((firstPoint - secondPoint).pow(2))
}