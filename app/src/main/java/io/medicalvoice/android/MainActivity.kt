package io.medicalvoice.android

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import io.medicalvoice.android.factory.MedicalViewModelFactory
import io.medicalvoice.android.ui.theme.MedicalVoiceTheme

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<VoiceViewModel> {
        MedicalViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermission() // TODO: проверка permission
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

    private fun checkPermission() {
        val permission = Manifest.permission.RECORD_AUDIO
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(
                this,
                setOf(permission).toTypedArray(),
                200
            )
        }
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