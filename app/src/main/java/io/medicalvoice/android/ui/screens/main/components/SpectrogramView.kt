package io.medicalvoice.android.ui.screens.main.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.medicalvoice.android.viewmodels.VoiceViewModel
import java.lang.Float.min

@Composable
fun SpectrogramView(
    modifier: Modifier,
    viewModel: VoiceViewModel
) {
    val coefficients by viewModel.spectrogramFlow.collectAsState(initial = emptyArray())

    Box(modifier = modifier) {
        if (coefficients.isEmpty()) {
            Text("Пустой список")
        } else {
            RectSpectrogram(coefficients)
        }
    }
}

@Composable
fun RectSpectrogram(spectrogram: Array<DoubleArray>) {
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val displayWidth = displayMetrics.widthPixels.toFloat()
    val displayHeight = displayMetrics.heightPixels.toFloat()

    val spectrogramSize = min(displayWidth, displayHeight).toInt()
    val spectrogramBitmap = Bitmap.createBitmap(
        spectrogramSize, // ширина битмапа равна размерности спектрограммы
        spectrogramSize, // высота битмапа равна размерности спектрограммы
        Bitmap.Config.RGB_565 // выбираем формат RGB_565, чтобы уменьшить размер изображения
    )
    val canvas = Canvas(spectrogramBitmap)
    canvas.drawColor(Color.BLACK) // заполняем белым фон
    val paint = Paint().apply {
        color = Color.WHITE // выбираем цвет отображения
    }
    // Рисуем нашу спектрограмму на битмапе
    for (freqIndex in spectrogram.indices) {
        for (timeIndex in spectrogram[freqIndex].indices) {
            // Определяем цвет пикселя на основе значения в спектрограмме
            val intensity = spectrogram[freqIndex][timeIndex]
            val color = Color.rgb(intensity.toInt(), intensity.toInt(), intensity.toInt())
            paint.color = color
            canvas.drawPoint(freqIndex.toFloat(), timeIndex.toFloat(), paint)
        }
    }
    // Отображаем спектрограмму в Compose
    val imageBitmap = spectrogramBitmap.asImageBitmap()
    Image(
        bitmap = imageBitmap, contentDescription = null, modifier = Modifier
            .size(spectrogramSize.dp)
            .clip(RoundedCornerShape(10.dp))
    )
}

private fun createBitmap(spectrogram: Array<DoubleArray>): ImageBitmap {
    val spectrogramBitmap = Bitmap.createBitmap(
        spectrogram.size,
        spectrogram[0].size,
        Bitmap.Config.RGB_565
    )

    val canvas = Canvas(spectrogramBitmap).apply {
        drawColor(Color.WHITE)
    }
    val paint = Paint().apply {
        color = Color.BLACK // выбираем цвет отображения
    }
    for (freqIndex in spectrogram.indices) {
        for (timeIndex in spectrogram[freqIndex].indices) {
            val intensity = spectrogram[freqIndex][timeIndex]
            val color = Color.rgb(intensity.toInt(), intensity.toInt(), intensity.toInt())
            paint.color = color
            canvas.drawPoint(freqIndex.toFloat(), timeIndex.toFloat(), paint)
        }
    }
    return spectrogramBitmap.asImageBitmap()
}

/**
 * Преобразование данных спектрограммы в Bitmap
 */
private fun spectrogramToBitmap(spectrogram: Array<DoubleArray>): ImageBitmap {
    val windowCount = spectrogram.size
    val levelCount = spectrogram[0].size

    // return ImageBitmap(
    //     width = windowCount,
    //     height = levelCount
    // ).apply {
    //     asAndroidBitmap().let {  canvas ->
    //         val paint = Paint().apply { isAntiAlias = true }
    //
    //         for (i in 0 until windowCount) {
    //             for (j in 0 until levelCount) {
    //                 val color = Color.rgb(
    //                     (spectrogram[i][j] * 255).toInt(),
    //                     (spectrogram[i][j] * 255).toInt(),
    //                     (spectrogram[i][j] * 255).toInt(),
    //                 )
    //                 paint.color = color
    //             }
    //         }
    //     }
    // }
    return Bitmap.createBitmap(windowCount, levelCount, Bitmap.Config.ARGB_8888).apply {
        setPixels(
            spectrogram.flatMap { row ->
                row.map { value ->
                    Color.rgb(
                        (value * 255).toInt(),
                        (value * 255).toInt(),
                        (value * 255).toInt()
                    )
                }
            }.toIntArray(),
            0,
            windowCount,
            0,
            0,
            windowCount,
            levelCount
        )
    }.asImageBitmap()
}