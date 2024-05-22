package com.ierusalem.androchat.features_tcp.tcp_host.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.ui.theme.AndroChatTheme

@Composable
internal fun TroubleshootUnableToStart(
    modifier: Modifier = Modifier,
    appName: String,
    isBroadcastError: Boolean,
    isProxyError: Boolean,
) {
    val context = LocalContext.current
    val errType =
        remember(
            isBroadcastError,
            isProxyError,
            context,
        ) {
            if (isBroadcastError && isProxyError) {
                context.getString(R.string.trouble_err_type_both)
            } else if (isProxyError) {
                context.getString(R.string.trouble_err_type_proxy)
            } else {
                context.getString(R.string.trouble_err_type_broadcast)
            }
        }

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.trouble_title, appName),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = stringResource(R.string.trouble_description, errType),
            fontWeight = FontWeight.W700,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )

        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = stringResource(R.string.trouble_double_check),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.W700,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (isBroadcastError) {
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = stringResource(R.string.trouble_broadcast_wifi_on),
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = stringResource(R.string.trouble_broadcast_wifi_not_connected),
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = stringResource(R.string.trouble_broadcast_wifi_restart),
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = stringResource(R.string.trouble_broadcast_password_length),
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = stringResource(R.string.trouble_broadcast_ssid_name),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        if (isProxyError) {
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = stringResource(R.string.trouble_proxy_already_used),
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = stringResource(R.string.trouble_proxy_port),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Preview
@Composable
private fun TroubleshootUnableToStartPreview() {
    AndroChatTheme {
        TroubleshootUnableToStart(
            appName = "Andor Chat",
            isBroadcastError = true,
            isProxyError = false
        )
    }
}
