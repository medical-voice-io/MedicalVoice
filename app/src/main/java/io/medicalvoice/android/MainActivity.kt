package io.medicalvoice.android

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import io.medicalvoice.android.app.MedicalVoiceApplication
import io.medicalvoice.android.extensions.checkAudioPermission
import io.medicalvoice.android.extensions.checkStoragePermission
import io.medicalvoice.android.factory.MedicalViewModelFactory

import io.medicalvoice.android.ui.theme.MedicalVoiceTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: VoiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAudioPermission(setOf(Manifest.permission.RECORD_AUDIO))
        checkStoragePermission(setOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))

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

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = { viewModel.startService() }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play),
                    contentDescription = null
                )
                Text("Record")
            }
        }
        IconButton(
            onClick = { viewModel.stopService() }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_stop),
                    contentDescription = null
                )
                Text("Stop")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MedicalVoiceTheme {
        VoiceControllerView(VoiceViewModel(MedicalVoiceApplication()))
    }
}