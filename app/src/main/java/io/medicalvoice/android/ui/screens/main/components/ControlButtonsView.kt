package io.medicalvoice.android.ui.screens.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.medicalvoice.android.R
import io.medicalvoice.android.ui.components.buttons.IconButton

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