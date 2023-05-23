package io.medicalvoice.android.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.medicalvoice.android.R
import io.medicalvoice.android.viewmodels.ConfigViewModel
import io.medicalvoice.android.viewmodels.VoiceViewModel

@Composable
fun ConfigView(
    viewModel: VoiceViewModel,
    configViewModel: ConfigViewModel
) {
    val isServiceRunning by viewModel.isServiceRunning.observeAsState(false)

    Column {
        TitleView(textRes = R.string.config_title)
        ConfigAudioRecorderView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            viewModel = configViewModel,
            isServiceRunning = isServiceRunning
        )
    }
}

@Composable
fun ConfigAudioRecorderView(
    modifier: Modifier = Modifier,
    viewModel: ConfigViewModel,
    isServiceRunning: Boolean
) {
    val scrollState = rememberScrollState()
    val selectedSampleRate by viewModel.selectedSampleRate.observeAsState(
        viewModel.sampleRate.keys.first()
    )
    val selectedEncoding by viewModel.selectedEncoding.observeAsState(
        viewModel.quantizationBitRates.keys.first()
    )
    val selectedChannelFormat by viewModel.selectedChannelFormat.observeAsState(
        viewModel.channels.keys.first()
    )

    Column(modifier = modifier.verticalScroll(scrollState)) {
        Spacer(modifier = Modifier.height(16.dp))
        HeaderView(textRes = R.string.sample_rate_header)
        Column {
            viewModel.sampleRate.forEach { sampleRate ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            enabled = !isServiceRunning,
                            selected = (selectedSampleRate == sampleRate.key),
                            onClick = { viewModel.selectSampleRate(sampleRate.key) }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        enabled = !isServiceRunning,
                        selected = (selectedSampleRate == sampleRate.key),
                        onClick = { viewModel.selectSampleRate(sampleRate.key) }
                    )
                    Text(
                        text = stringResource(id = sampleRate.value),
                        modifier = Modifier
                            .padding(end = 16.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        HeaderView(textRes = R.string.encoding_header)
        Column {
            viewModel.quantizationBitRates.forEach { quantizationBitRate ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            enabled = !isServiceRunning,
                            selected = (selectedEncoding == quantizationBitRate.key),
                            onClick = { viewModel.selectEncoding(quantizationBitRate.key) }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        enabled = !isServiceRunning,
                        selected = (selectedEncoding == quantizationBitRate.key),
                        onClick = { viewModel.selectEncoding(quantizationBitRate.key) }
                    )
                    Text(
                        text = stringResource(id = quantizationBitRate.value),
                        modifier = Modifier
                            .padding(end = 16.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        HeaderView(textRes = R.string.channel_header)
        Column {
            viewModel.channels.forEach { channel ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            enabled = !isServiceRunning,
                            selected = (selectedChannelFormat == channel.key),
                            onClick = { viewModel.selectChannelFormat(channel.key) }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        enabled = !isServiceRunning,
                        selected = (selectedChannelFormat == channel.key),
                        onClick = { viewModel.selectChannelFormat(channel.key) }
                    )
                    Text(
                        text = stringResource(id = channel.value),
                        modifier = Modifier
                            .padding(end = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TitleView(
    @StringRes textRes: Int
) = Text(
    text = stringResource(textRes),
    fontSize = 24.sp,
    modifier = Modifier.padding(vertical = 8.dp)
)

@Composable
fun HeaderView(
    @StringRes textRes: Int
) = Text(
    text = stringResource(textRes),
    fontSize = 20.sp,
    modifier = Modifier.padding(vertical = 4.dp)
)