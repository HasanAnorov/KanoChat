package com.ierusalem.androchat.features_tcp.tcp_host.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ierusalem.androchat.R

private enum class TroubleshootDialogContentTypes {
    STEPS
}

@Composable
internal fun TroubleshootDialog(
    modifier: Modifier = Modifier,
    appName: String,
    isBroadcastError: Boolean,
    isProxyError: Boolean,
    onDismiss: () -> Unit,
) {

    Dialog(
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
        onDismissRequest = onDismiss,
    ) {
        Card(
            modifier = modifier.padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.elevatedCardElevation(),
            colors = CardDefaults.elevatedCardColors(),
        ) {
            Column {
                LazyColumn(
                    modifier =
                    Modifier.weight(
                        weight = 1F,
                        fill = false,
                    ),
                ) {
                    item(
                        contentType = TroubleshootDialogContentTypes.STEPS,
                    ) {
                        TroubleshootUnableToStart(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            appName = appName,
                            isBroadcastError = isBroadcastError,
                            isProxyError = isProxyError,
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(
                        modifier = Modifier.weight(1F),
                    )

                    TextButton(
                        onClick = {
                            onDismiss()
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewTroubleshootDialog(
    isBroadcastError: Boolean,
    isProxyError: Boolean,
) {
    TroubleshootDialog(
        appName = "Andro Chat",
        isBroadcastError = isBroadcastError,
        isProxyError = isProxyError,
        onDismiss = {},
    )
}

@Preview
@Composable
private fun PreviewTroubleshootDialogNone() {
    PreviewTroubleshootDialog(
        isBroadcastError = false,
        isProxyError = false,
    )
}

@Preview
@Composable
private fun PreviewTroubleshootDialogBroadcast() {
    PreviewTroubleshootDialog(
        isBroadcastError = true,
        isProxyError = false,
    )
}

@Preview
@Composable
private fun PreviewTroubleshootDialogProxy() {
    PreviewTroubleshootDialog(
        isBroadcastError = false,
        isProxyError = true,
    )
}

@Preview
@Composable
private fun PreviewTroubleshootDialogBoth() {
    PreviewTroubleshootDialog(
        isBroadcastError = true,
        isProxyError = true,
    )
}