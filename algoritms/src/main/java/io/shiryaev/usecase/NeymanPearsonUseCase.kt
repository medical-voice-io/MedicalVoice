package io.shiryaev.usecase

import android.util.Log
import io.shiryaev.method.Frame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

typealias NoiseFrames = List<Frame>
typealias SecondFrames = List<Frame>

class NeymanPearsonUseCase @Inject constructor() {

    suspend operator fun invoke(
        frames: List<Frame>,
        b: Double
    ): List<Frame> = withContext(Dispatchers.Default) {
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

        // val noiseFrames = mutableListOf<Frame>()
        // val secondFrames = mutableListOf<Frame>()

        Log.i("TRESHOLD", "Порог: $threshold")
        frames.map { frame ->
            frame.copy(
                isNoise = frame.power < threshold
            )
            // if (frame.power > threshold) {
            //     // Переместить во 2 класс
            //     secondFrames.add(frame)
            // } else {
            //     // Оставить в 3 классе
            //     noiseFrames.add(frame)
            // }
        }

        // secondFrames to noiseFrames
    }
}