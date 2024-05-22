package com.ierusalem.androchat.features_tcp.tcp_host

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.features_tcp.tcp.TcpScreenEvents
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpScreenUiState
import com.ierusalem.androchat.features_tcp.tcp_host.components.HotspotButton
import com.ierusalem.androchat.features_tcp.tcp_host.components.StatusView
import com.ierusalem.androchat.features_tcp.tcp_host.components.WifiLazyItem
import com.ierusalem.androchat.ui.theme.AndroChatTheme

@Composable
fun HotSpotContent(
    modifier: Modifier = Modifier,
    eventHandler: (TcpScreenEvents) -> Unit,
    state: TcpScreenUiState,
) {
    LazyColumn(modifier = modifier) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HotspotButton(
                    modifier = Modifier
                        .weight(1F)
                        .padding(start = 16.dp)
                        .padding(top = 16.dp),
                    onClick = { eventHandler(TcpScreenEvents.CreateServerClick) },
                    title = state.hotspotTitleStatus.status
                )
                Spacer(Modifier.width(8.dp))
                HotspotButton(
                    modifier = Modifier
                        .weight(1F)
                        .padding(end = 16.dp)
                        .padding(top = 16.dp),
                    onClick = { eventHandler(TcpScreenEvents.ConnectToServerClick) },
                    title = (R.string.connect)
                )
            }
        }
        item {
            StatusView(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceDim),
                isWifiEnabled = state.isWifiOn,
                connectionStatus = state.connectionStatus,
                connectionRole = state.isOwner,
                groupAddress = state.groupOwnerAddress
            )
        }
        item {
            HotspotButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                onClick = { eventHandler(TcpScreenEvents.DiscoverWifiClick) },
                title = state.wifiDiscoveryStatus.res
            )
        }
        item {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceDim)
            ) {
                items(state.availableWifiNetworks) { wifiDevice ->
                    WifiLazyItem(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        onClick = {
                            eventHandler(TcpScreenEvents.OnConnectToWifiClick(wifiDevice))
                        },
                        wifiName = wifiDevice.deviceName
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun HotspotContentPreview() {
    AndroChatTheme {
        HotSpotContent(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            eventHandler = {},
            state = TcpScreenUiState()
        )
    }
}