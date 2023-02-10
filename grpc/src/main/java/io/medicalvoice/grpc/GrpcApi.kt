package io.medicalvoice.grpc

import io.grpc.ManagedChannel
import io.medicalvoice.grpc.proto.MedicalVoiceApiGrpcKt.MedicalVoiceApiCoroutineStub
import io.medicalvoice.grpc.proto.VoiceRequest
import kotlinx.coroutines.flow.Flow
import java.io.Closeable
import java.util.concurrent.TimeUnit

class MedicalVoiceClient(
    private val channel: ManagedChannel
) : Closeable {

    private val stub: MedicalVoiceApiCoroutineStub = MedicalVoiceApiCoroutineStub(channel)

    suspend fun myFunc(voiceData: Flow<VoiceRequest>) {
        stub.sendVoiceData(voiceData)
    }

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}