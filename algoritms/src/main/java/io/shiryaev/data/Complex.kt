package io.shiryaev.data

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Простая реализация комплексного числа
 *
 * @property real действительная часть
 * @property imag мнимая часть
 */
data class Complex(
    val real: Double,
    val imag: Double,
) {
    fun magnitude(): Double = sqrt(real.pow(2) + imag.pow(2))

    operator fun plus(other: Complex): Complex {
        return Complex(this.real + other.real, this.imag + other.imag)
    }

    operator fun minus(other: Complex): Complex {
        return Complex(this.real - other.real, this.imag - other.imag)
    }

    operator fun times(other: Complex): Complex {
        val realPart = this.real * other.real - this.imag * other.imag
        val imagPart = this.real * other.imag + this.imag * other.real
        return Complex(realPart, imagPart)
    }

    companion object {
        fun polar(r: Double, theta: Double) = Complex(
            real = r * cos(theta),
            imag = r * sin(theta),
        )
    }
}
