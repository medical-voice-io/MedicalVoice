package io.shiryaev.usecase

import io.shiryaev.method.Frame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

class NeymanPearsonUseCase @Inject constructor() {

    suspend operator fun invoke(
        frames: List<Frame>,
        b: Double
    ) = withContext(Dispatchers.Default) {
        val noisePowers = frames.map { frame -> frame.power }

        // Математическое отклонение
        val noiseMean = noisePowers.average()

        // Среднеквадратическое отклонение
        val noiseStdDev = sqrt(
            noisePowers
                .map { power -> power - noiseMean }
                .map { different -> different.pow(2) }
                .average()
        )

        // Вычисление порога U по критерию Неймана-Пирсона
        val threshold = noiseMean + b * noiseStdDev

        return frames.map { frame ->
            if (frame.power > threshold) {
                // Переместить во 2 класс
            } else {
                // Оставить в 3 классе
            }
        }
    }
}