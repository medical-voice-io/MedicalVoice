package io.medicalvoice.android

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import dagger.hilt.android.AndroidEntryPoint
import io.medicalvoice.android.extensions.checkAudioPermission
import io.medicalvoice.android.ui.theme.MedicalVoiceTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: VoiceViewModel by viewModels()

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
                    VoiceControllerView(viewModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            viewModel.bindService()
        } catch (_: Exception) {}
    }

    override fun onStop() {
        viewModel.unbindService()
        super.onStop()
    }
}

@Composable
fun VoiceControllerView(
    viewModel: VoiceViewModel
) {
    val isServiceRunning by viewModel.isServiceRunning.observeAsState(false)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { viewModel.startService() },
            // enabled = !isServiceRunning // TODO: вернуть, когда пофиксится bindService
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_play),
                contentDescription = null
            )
            Text("Start service")
        }
        Button(
            onClick = { viewModel.stopService() },
            // enabled = isServiceRunning
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_stop),
                contentDescription = null
            )
            Text("Stop service")
        }
    }
}

// @Preview(showBackground = true)
// @Composable
// fun DefaultPreview() {
//     MedicalVoiceTheme {
//         VoiceControllerView(VoiceViewModel())
//     }
// }