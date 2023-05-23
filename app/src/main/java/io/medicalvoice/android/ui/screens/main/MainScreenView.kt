package io.medicalvoice.android.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.medicalvoice.android.ui.screens.ConfigView
import io.medicalvoice.android.ui.screens.main.components.ControlButtonsView
import io.medicalvoice.android.ui.screens.main.components.SpectrogramView
import io.medicalvoice.android.viewmodels.ConfigViewModel
import io.medicalvoice.android.viewmodels.VoiceViewModel

@Composable
fun MainScreenView(
    viewModel: VoiceViewModel,
    configViewModel: ConfigViewModel
) {
    val isServiceRunning by viewModel.isServiceRunning.observeAsState(false)
    val sampleRate by configViewModel.selectedSampleRate.observeAsState(
        configViewModel.sampleRate.keys.first()
    )
    val encoding by configViewModel.selectedEncoding.observeAsState(
        configViewModel.quantizationBitRates.keys.first()
    )
    val channelFormat by configViewModel.selectedChannelFormat.observeAsState(
        configViewModel.channels.keys.first()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (!isServiceRunning) {
                ConfigView(
                    viewModel = viewModel,
                    configViewModel = configViewModel
                )
            } else {
                SpectrogramView(
                    modifier = Modifier
                        .fillMaxWidth(),
                    viewModel = viewModel
                )
            }
        }
        ControlButtonsView(
            isServiceRunning = isServiceRunning,
            onStartClick = {
                viewModel.startService(
                    sampleRate = sampleRate,
                    encoding = encoding,
                    channelFormat = channelFormat
                )
            },
            onStopClick = viewModel::stopService
        )
    }
}