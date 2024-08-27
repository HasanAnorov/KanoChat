package com.ierusalem.androchat.features_local.tcp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpCloseDialogReason


@Composable
fun TcpCloseDialog(
    modifier: Modifier = Modifier,
    onDismissDialog: () -> Unit,
    onConfirmDialog: (Boolean) -> Unit,
    reason: TcpCloseDialogReason
) {

    var doNotShowMeAgain by rememberSaveable { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onDismissDialog() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
        content = {
            Surface(
                modifier = modifier,
                shape = RoundedCornerShape(8.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = stringResource(id = reason.reason),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Start,
                        text = stringResource(id = reason.description),
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        modifier = Modifier.padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(1F),
                            text = stringResource(R.string.don_t_ask_me_again),
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Checkbox(
                            checked = doNotShowMeAgain,
                            onCheckedChange = { doNotShowMeAgain = !doNotShowMeAgain }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1F)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color = MaterialTheme.colorScheme.primary)
                                .clickable { onDismissDialog() },
                            content = {
                                Text(
                                    text = stringResource(R.string.cancel),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 16.dp,
                                            vertical = 16.dp
                                        ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    textAlign = TextAlign.Center
                                )
                            },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .weight(1F)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color = MaterialTheme.colorScheme.primary)
                                .clickable {
                                    onConfirmDialog(doNotShowMeAgain)
                                    onDismissDialog()
                                },
                            content = {
                                Text(
                                    text = stringResource(R.string.confirm),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 16.dp,
                                            vertical = 16.dp
                                        ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    textAlign = TextAlign.Center
                                )
                            },
                        )
                    }
                }
            }
        }
    )
}

@Preview
@Composable
private fun PreviewLight() {
    AndroChatTheme {
        TcpCloseDialog(
            reason = TcpCloseDialogReason.ExistingMessages,
            onDismissDialog = {},
            onConfirmDialog = { _ -> }
        )
    }
}

@Preview
@Composable
private fun PreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        TcpCloseDialog(
            reason = TcpCloseDialogReason.ExistingConnection,
            onDismissDialog = {},
            onConfirmDialog = { _ -> }
        )
    }
}
