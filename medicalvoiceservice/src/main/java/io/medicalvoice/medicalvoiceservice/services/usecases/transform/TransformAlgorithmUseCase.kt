package io.medicalvoice.medicalvoiceservice.services.usecases.transform

import io.medicalvoice.algoritrms.wt.Cwt
import io.medicalvoice.algoritrms.wt.Fwt
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * UseCase для различных преобразований входных данных из микрофона
 */
class TransformAlgorithmUseCase @Inject constructor(
    private val fwtTransform: Fwt,
    private val cwtTransform: Cwt
) : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Dispatchers.Default + Job() + CoroutineName("TransformAlgorithmUseCase")

    private val _coefficientsFlow = MutableSharedFlow<Array<DoubleArray>>()
    val coefficients = _coefficientsFlow.asSharedFlow()

    suspend fun getCoefficients(buffer: ShortArray) = withContext(coroutineContext) {
        val resultCoefficients = fwtTransform.directTransform(buffer.map { it.toFloat() })
        getSpectrogram(resultCoefficients)
    }

    suspend fun getSpectrogram(buffer: List<Float>) = withContext(coroutineContext) {
        val spectrogram = cwtTransform.directTransform(buffer)
        _coefficientsFlow.emit(spectrogram)
    }
}