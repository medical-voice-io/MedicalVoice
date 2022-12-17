package io.medicalvoice.medicalvoiceservice.services.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.medicalvoice.medicalvoiceservice.services.AudioRecorder
import io.medicalvoice.medicalvoiceservice.services.AudioRecorderInteractor
import io.medicalvoice.medicalvoiceservice.services.AudioRecorderRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MedicalVoiceServiceModule {

    @Provides
    @Singleton
    fun provideAudioRecorderInteractor(
        audioRecorderRepository: AudioRecorderRepository
    ): AudioRecorderInteractor = AudioRecorderInteractor(
        audioRecorderRepository = audioRecorderRepository
    )

    @Provides
    @Singleton
    fun provideAudioRecorderRepository(
        audioRecorder: AudioRecorder
    ): AudioRecorderRepository = AudioRecorderRepository(
        audioRecorder = audioRecorder
    )

    @Provides
    @Singleton
    fun provideAudioRecorder(): AudioRecorder = AudioRecorder()
}