package com.ierusalem.androchat.features_local.tcp.presentation.tcp_networking.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.components.baselineHeight
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.ui.theme.MontserratFontFamily

@Composable
fun StatusProperty(
    modifier: Modifier = Modifier,
    status: String,
    @StringRes state: Int,
    stateColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Column(
        modifier = modifier.padding(bottom = 16.dp)
    ) {
        Text(
            text = status,
            fontFamily = MontserratFontFamily,
            modifier = Modifier.baselineHeight(20.dp),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(id = state),
            modifier = Modifier.baselineHeight(20.dp),
            style = MaterialTheme.typography.titleMedium,
            color = stateColor
        )
    }
}

@Preview
@Composable
private fun PreviewPort() {
    AndroChatTheme {
        Surface {
            StatusProperty(
                state = R.string.connect,
                status = "Anorov"
            )
        }
    }
}

@Preview
@Composable
private fun PreviewPortDark() {
    AndroChatTheme(isDarkTheme = true) {
        Surface {
            StatusProperty(
                state = R.string.connect,
                status = "Anorov"
            )
        }
    }
}