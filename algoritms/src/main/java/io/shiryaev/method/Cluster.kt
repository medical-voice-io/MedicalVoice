package io.shiryaev.method

/**
 * @property centroid
 * @property points
 */
data class Cluster(
    val centroid: Double,
    val points: List<Frame>
)
