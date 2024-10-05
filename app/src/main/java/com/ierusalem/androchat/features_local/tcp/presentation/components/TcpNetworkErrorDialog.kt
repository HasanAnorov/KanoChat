package com.ierusalem.androchat.features_local.tcp.presentation.components

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme

@Composable
fun NetworkErrorDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    @StringRes dialogTitle: Int,
    @StringRes dialogText: Int,
    icon: Painter,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = stringResource(id = dialogTitle))
        },
        text = {
            Text(text = stringResource(id = dialogText))
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(stringResource(R.string.close_lowercase))
            }
        },
        dismissButton = {}
    )
}

@Preview
@Composable
private fun Preview_TcpNetworkErrorDialog() {
    AndroChatTheme {
        NetworkErrorDialog(
            onDismissRequest = { },
            onConfirmation = {},
            dialogTitle = R.string.create_a_server,
            dialogText = R.string.couldn_t_connect_to_chosen_wifi_device,
            icon = painterResource(id = R.drawable.wifi_off)
        )
    }
}

@Preview
@Composable
private fun Preview_Dark_TcpNetworkErrorDialog() {
    AndroChatTheme(isDarkTheme = true) {
        NetworkErrorDialog(
            onDismissRequest = { },
            onConfirmation = {},
            dialogTitle = R.string.create_a_server,
            dialogText = R.string.couldn_t_connect_to_chosen_wifi_device,
            icon = painterResource(id = R.drawable.wifi_off)
        )
    }
}