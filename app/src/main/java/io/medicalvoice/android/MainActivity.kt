package io.medicalvoice.android

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint
import io.medicalvoice.android.app.MedicalVoiceApplication
import io.medicalvoice.android.components.WaveVisualization
import io.medicalvoice.android.extensions.checkAudioPermission
import io.medicalvoice.android.ui.components.buttons.IconButton
import io.medicalvoice.android.ui.theme.MedicalVoiceTheme
import io.medicalvoice.android.viewmodels.ConfigViewModel
import io.medicalvoice.android.viewmodels.VoiceViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: VoiceViewModel by viewModels()
    private val configViewModel: ConfigViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAudioPermission(setOf(Manifest.permission.RECORD_AUDIO))

        setContent {
            MedicalVoiceTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column {
                        val audioFramesState by viewModel.audioFramesFlow.collectAsState()

                        if (audioFramesState.isNotEmpty()) {
                            WaveVisualization(
                                frames = audioFramesState
                            )
                        }
                        VoiceControllerView(
                            viewModel,
                            configViewModel
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.bindService()
    }

    override fun onStop() {
        super.onStop()
        viewModel.unbindService()
    }
}

@Composable
fun VoiceControllerView(
    viewModel: VoiceViewModel,
    configViewModel: ConfigViewModel
) {
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()
    val sampleRate by configViewModel.selectedSampleRate.collectAsState()
    val encoding by configViewModel.selectedEncoding.collectAsState()
    val channelFormat by configViewModel.selectedChannelFormat.collectAsState()
    val threshold by configViewModel.threshold.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        TitleView(textRes = R.string.config_title)
        ConfigAudioRecorderView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            viewModel = configViewModel,
            isServiceRunning = isServiceRunning
        )
        ControlButtonsView(
            isServiceRunning = isServiceRunning,
            onStartClick = {
                viewModel.startService(
                    sampleRate = sampleRate,
                    encoding = encoding,
                    channelFormat = channelFormat,
                    threshold = threshold.toDoubleOrNull() ?: 1.0
                )
            },
            onStopClick = viewModel::stopService
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
    val selectedSampleRate by viewModel.selectedSampleRate.collectAsState()
    val selectedEncoding by viewModel.selectedEncoding.collectAsState()
    val selectedChannelFormat by viewModel.selectedChannelFormat.collectAsState()
    val threshold by viewModel.threshold.collectAsState()

    Column(modifier = modifier.verticalScroll(scrollState)) {
        Spacer(modifier = Modifier.height(16.dp))
        HeaderView(textRes = R.string.threshold_header)
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = threshold,
            onValueChange = viewModel::onThresholdTextChange,
            label = {
                Text("Коэффициент порога")
            }
        )
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

@Composable
fun ControlButtonsView(
    isServiceRunning: Boolean = false,
    onStartClick: () -> Unit = {},
    onStopClick: () -> Unit = {}
) = Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceEvenly
) {
    IconButton(
        textRes = R.string.start_voice_service_text,
        iconRes = R.drawable.ic_play,
        enabled = !isServiceRunning,
        onClick = onStartClick
    )
    IconButton(
        textRes = R.string.stop_voice_service_text,
        iconRes = R.drawable.ic_stop,
        enabled = isServiceRunning,
        onClick = onStopClick
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MedicalVoiceTheme {
        VoiceControllerView(
            VoiceViewModel(MedicalVoiceApplication()),
            ConfigViewModel(MedicalVoiceApplication())
        )
    }
}