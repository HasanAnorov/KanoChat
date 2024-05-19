package com.ierusalem.androchat.features_tcp.tcp_client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.features_tcp.tcp.TcpScreenEvents
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpScreenUiState
import com.ierusalem.androchat.features_tcp.tcp_server.components.HotspotButton
import com.ierusalem.androchat.ui.theme.AndroChatTheme

@Composable
fun ClientContent(
    modifier: Modifier = Modifier,
    eventHandler: (TcpScreenEvents) -> Unit,
    state: TcpScreenUiState
) {
    LazyColumn(modifier = modifier) {
        item {
            HotspotButton(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                onClick = { eventHandler(TcpScreenEvents.ConnectToServerClick) },
                title = state.clientTitleStatus.status,
            )
        }
    }
}

@Preview
@Composable
private fun ClientContentPreview() {
    AndroChatTheme {
        ClientContent(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            eventHandler = {},
            state = TcpScreenUiState()
        )
    }
}