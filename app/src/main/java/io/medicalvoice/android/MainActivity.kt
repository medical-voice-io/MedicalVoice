package io.medicalvoice.android

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import io.medicalvoice.android.extensions.checkAudioPermission
import io.medicalvoice.android.ui.screens.main.MainScreenView
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
                    MainScreenView(
                        viewModel = viewModel,
                        configViewModel = configViewModel
                    )
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
//
// @Preview(showBackground = true)
// @Composable
// fun DefaultPreview() {
//     MedicalVoiceTheme {
//         VoiceControllerView(
//             VoiceViewModel(MedicalVoiceApplication()),
//             ConfigViewModel(MedicalVoiceApplication())
//         )
//     }
// }