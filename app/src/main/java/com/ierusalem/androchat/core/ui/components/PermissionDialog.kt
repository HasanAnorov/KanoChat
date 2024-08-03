package com.ierusalem.androchat.core.ui.components

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme

@Composable
fun PermissionDialog(
    permissionTextProvider: PermissionTextProvider,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
            Text(
                text = stringResource(R.string.permission_required),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Text(
                text = permissionTextProvider.getDescription(
                    context = context,
                    isPermanentlyDeclined = isPermanentlyDeclined
                )
            )
        },
        modifier = modifier
    )
}

interface PermissionTextProvider {
    fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String
}

class RecordAudioPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            context.getString(R.string.record_audio_denied)
        } else {
            context.getString(R.string.record_audio_permission_request)
        }
    }
}

class ReadContactsPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            context.getString(R.string.contacts_denied)
        } else {
            context.getString(R.string.contacts_permission_request)
        }
    }
}

class NearbyWifiDevicesPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            context.getString(R.string.nearby_wifi_devices_denied)
        } else {
            context.getString(R.string.nearby_wifi_devices_permission_request)
        }
    }
}

class CoarseLocationPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            context.getString(R.string.coarse_location_denied)
        } else {
            context.getString(R.string.coarse_location_permission_request)
        }
    }
}

class FineLocationPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            context.getString(R.string.fine_location_denied)
        } else {
            context.getString(R.string.fine_location_permission_request)
        }
    }
}

@Preview
@Composable
fun PermissionDialog_Preview() {
    AndroChatTheme {
        PermissionDialog(
            isPermanentlyDeclined = false,
            onDismiss = { },
            onOkClick = { },
            onGoToAppSettingsClick = { },
            permissionTextProvider = RecordAudioPermissionTextProvider()
        )
    }
}

@Preview
@Composable
fun PermissionDialog_Preview_Dark() {
    AndroChatTheme(isDarkTheme = true) {
        PermissionDialog(
            isPermanentlyDeclined = true,
            onDismiss = { },
            onOkClick = { },
            onGoToAppSettingsClick = { },
            permissionTextProvider = ReadContactsPermissionTextProvider()
        )
    }
}