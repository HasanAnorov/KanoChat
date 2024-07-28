package com.ierusalem.androchat.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme

@Composable
fun PermissionDialog(
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = {
                    HorizontalDivider()
                    Text(
                        text = if (isPermanentlyDeclined) {
                            stringResource(R.string.give_permission)
                        } else {
                            stringResource(R.string.OK)
                        },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isPermanentlyDeclined) {
                                    onGoToAppSettingsClick()
                                } else {
                                    onOkClick()
                                }
                            }
                            .padding(16.dp)
                    )
                }
            )
        },
        title = {
            Text(text = stringResource(R.string.permission_required), color = MaterialTheme.colorScheme.onBackground)
        },
        text = {
            Text(
                text = if(isPermanentlyDeclined) {
                    stringResource(R.string.contacts_denied)
                } else {
                    stringResource(R.string.contacts_permission_request)
                },
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        modifier = modifier
    )
}

@Preview
@Composable
fun PermissionDialog_Preview(){
    AndroChatTheme {
        PermissionDialog(
            isPermanentlyDeclined = false,
            onDismiss = { },
            onOkClick = { },
            onGoToAppSettingsClick = { }
        )
    }
}
@Preview
@Composable
fun PermissionDialog_Preview_Dark(){
    AndroChatTheme(isDarkTheme = true) {
        PermissionDialog(
            isPermanentlyDeclined = true,
            onDismiss = { },
            onOkClick = { },
            onGoToAppSettingsClick = { }
        )
    }
}