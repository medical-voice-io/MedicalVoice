package io.shiryaev.method

/**
 * Окно
 *
 * @property id номер окна
 * @property power мощность сигнала в текущем окне
 * @property isNoise является ли окно шумом
 */
data class Frame(
    val id: Int,
    val power: Double,
    val isNoise: Boolean = false
)