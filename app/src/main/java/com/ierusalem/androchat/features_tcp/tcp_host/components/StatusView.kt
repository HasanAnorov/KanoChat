package com.ierusalem.androchat.features_tcp.tcp_host.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.features_tcp.tcp.domain.ConnectionStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.OwnerStatusState
import com.ierusalem.androchat.ui.theme.AndroChatTheme

@Composable
fun StatusView(
    modifier: Modifier = Modifier,
    isWifiEnabled: Boolean = false,
    connectionStatus: ConnectionStatus = ConnectionStatus.Idle,
    connectionRole: OwnerStatusState = OwnerStatusState.Idle,
    groupAddress: String? = null
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier.padding(top = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.onBackground,
                text = "WiFi Status: ",
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.onBackground,
                text = if (isWifiEnabled) stringResource(R.string.wifi_enabled) else stringResource(
                    R.string.wifi_disabled
                ),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        Column(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.onBackground,
                text = "Connection Details: ",
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    color = MaterialTheme.colorScheme.onBackground,
                    text = "Status: ",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                //connected, running, not running
                Text(
                    modifier = Modifier
                        .weight(1F)
                        .padding(end = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                    text = stringResource(id = connectionStatus.status),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    color = MaterialTheme.colorScheme.onBackground,
                    text = "Role: ",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                //owner, client
                Text(
                    modifier = Modifier
                        .weight(1F)
                        .padding(end = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                    text = stringResource(id = connectionRole.status),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    color = MaterialTheme.colorScheme.onBackground,
                    text = "Group Address: ",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                //here it's group address
                Text(
                    modifier = Modifier
                        .weight(1F)
                        .padding(end = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                    text = groupAddress ?: stringResource(id = R.string.waiting_for_connection),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewStatusView() {
    AndroChatTheme {
        Surface {
            StatusView(
                modifier = Modifier.fillMaxWidth(),

                )
        }
    }
}