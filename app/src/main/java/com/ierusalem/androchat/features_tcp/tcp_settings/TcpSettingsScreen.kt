package com.ierusalem.androchat.features_tcp.tcp_settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ierusalem.androchat.ui.theme.AndroChatTheme

@Composable
fun TcpSettingsScreen() {
    Scaffold {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            item {
                TcpSettingsAppBar(
                    onNavIconClick = {  },
                )
            }
        }
    }
}

@Preview
@Composable
private fun TcpSettingsPreview() {
    AndroChatTheme {
        TcpSettingsScreen()
    }
}