package com.ierusalem.androchat.features_tcp.tcp_connections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.features_tcp.tcp.TcpScreenEvents
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpScreenUiState
import com.ierusalem.androchat.features_tcp.tcp_host.components.StatusProperty
import com.ierusalem.androchat.features_tcp.tcp_host.components.WifiLazyItem
import com.ierusalem.androchat.ui.components.baselineHeight
import com.ierusalem.androchat.ui.theme.AndroChatTheme
import com.ierusalem.androchat.ui.theme.MontserratFontFamily

@Composable
fun ConnectionsContent(
    modifier: Modifier = Modifier,
    eventHandler: (TcpScreenEvents) -> Unit,
    state: TcpScreenUiState
) {
    LazyColumn(modifier = modifier) {
        item {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceDim.copy(0.2F)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceDim.copy(0.4F)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        text = stringResource(R.string.connections_details),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.background,
                    thickness = 1.dp
                )
                StatusProperty(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    status = stringResource(R.string.connection_status),
                    state = state.connectionStatus.status
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 1.dp)
                StatusProperty(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    status = stringResource(R.string.role),
                    state = state.isOwner.status
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.server_address),
                        fontFamily = MontserratFontFamily,
                        modifier = Modifier.baselineHeight(20.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.groupOwnerAddress
                            ?: stringResource(id = R.string.waiting_for_connection),
                        modifier = Modifier.baselineHeight(24.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 16.dp)
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceDim.copy(0.2F)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceDim.copy(0.4F)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        text = stringResource(R.string.connected_devices_will_display_here),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.background,
                    thickness = 1.dp
                )
                if (state.connectedWifiNetworks.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .padding(top = 16.dp)
                            .height(200.dp)
                    ) {
                        items(state.connectedWifiNetworks) { wifiDevice ->
                            WifiLazyItem(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                onClick = {
                                    eventHandler(TcpScreenEvents.OnConnectToWifiClick(wifiDevice))
                                },
                                wifiName = wifiDevice.deviceName
                            )
                        }
                    }
                }else{
                    Text(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .padding(horizontal = 10.dp),
                        text = stringResource(R.string.connections_are_not_established_yet),
                        fontFamily = MontserratFontFamily,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            }
        }
    }
}

@Preview(locale = "ru")
@Composable
private fun ClientContentPreview() {
    AndroChatTheme {
        ConnectionsContent(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            eventHandler = {},
            state = TcpScreenUiState()
        )
    }
}

@Preview
@Composable
private fun ClientContentPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        ConnectionsContent(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            eventHandler = {},
            state = TcpScreenUiState()
        )
    }
}