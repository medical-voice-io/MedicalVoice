package io.medicalvoice.android.ui.components.buttons

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.material.IconButton as AndroidIconButton

@Composable
fun IconButton(
    @StringRes textRes: Int,
    @DrawableRes iconRes: Int,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    AndroidIconButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null
            )
            Text(text = stringResource(id = textRes))
        }
    }
}