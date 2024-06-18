package com.ierusalem.androchat.features_tcp.tcp_settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme

@Composable
fun TcpSettingsScreen(
    onNavIconClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            TcpSettingsAppBar(
                onNavIconClick = onNavIconClick,
            )
        }
    }
}

@Preview
@Composable
private fun TcpSettingsPreview() {
    AndroChatTheme {
        TcpSettingsScreen(
            onNavIconClick = {}
        )
    }
}