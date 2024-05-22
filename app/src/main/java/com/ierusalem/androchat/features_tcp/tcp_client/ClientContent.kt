package com.ierusalem.androchat.features_tcp.tcp_client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ierusalem.androchat.features_tcp.tcp.TcpScreenEvents
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpScreenUiState
import com.ierusalem.androchat.ui.theme.AndroChatTheme

@Composable
fun ClientContent(
    modifier: Modifier = Modifier,
    eventHandler: (TcpScreenEvents) -> Unit,
    state: TcpScreenUiState
) {
    LazyColumn(modifier = modifier) {

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