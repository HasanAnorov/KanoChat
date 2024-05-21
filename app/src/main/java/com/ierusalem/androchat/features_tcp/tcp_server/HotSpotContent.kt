package com.ierusalem.androchat.features_tcp.tcp_server

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.features_tcp.tcp.TcpScreenEvents
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpScreenUiState
import com.ierusalem.androchat.features_tcp.tcp_server.components.HotspotButton
import com.ierusalem.androchat.ui.theme.AndroChatTheme

@Composable
fun HotSpotContent(
    modifier: Modifier = Modifier,
    eventHandler: (TcpScreenEvents) -> Unit,
    state: TcpScreenUiState,
) {
    LazyColumn(modifier = modifier) {
        item {
            HotspotButton(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                onClick = { eventHandler(TcpScreenEvents.OpenHotspotClick) },
                title = state.hotspotTitleStatus.status
            )
        }
        item {
            HotspotButton(
                modifier = Modifier
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
                items(state.availableWifiNetworks){
                    Text(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        text = it.deviceName,
                        style = MaterialTheme.typography.titleMedium
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