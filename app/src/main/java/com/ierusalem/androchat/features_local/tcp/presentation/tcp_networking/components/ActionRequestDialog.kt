package com.ierusalem.androchat.features_local.tcp.presentation.tcp_networking.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme

@Composable
fun ActionRequestDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    @StringRes dialogTitle: Int,
    @StringRes dialogText: Int,
    @StringRes positiveButtonRes: Int = R.string.confirm,
    @StringRes negativeButtonRes: Int = R.string.dismiss,
    icon: ImageVector,
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
                Text(text = stringResource(id = positiveButtonRes))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(id = negativeButtonRes))
            }
        }
    )
}

@Preview
@Composable
private fun Preview_Light() {
    AndroChatTheme {
        ActionRequestDialog(
            onDismissRequest = { },
            onConfirmation = { },
            dialogTitle = R.string.wifi_not_enabled,
            dialogText = R.string.wifi_should_be_enabled_to_perform_this_action,
            icon = Icons.Default.WifiOff,
            positiveButtonRes = R.string.enable,
            negativeButtonRes = R.string.dismiss
        )
    }
}

@Preview
@Composable
private fun Preview_Dark() {
    AndroChatTheme(isDarkTheme = true) {
        ActionRequestDialog(
            onDismissRequest = { },
            onConfirmation = { },
            dialogTitle = R.string.wifi_not_enabled,
            dialogText = R.string.wifi_should_be_enabled_to_perform_this_action,
            icon = Icons.Default.WifiOff,
            positiveButtonRes = R.string.enable,
            negativeButtonRes = R.string.dismiss
        )
    }
}
