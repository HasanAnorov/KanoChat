package com.ierusalem.androchat.features_tcp.tcp_settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ierusalem.androchat.core.ui.components.AndroChatAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TcpSettingsAppBar(
    modifier: Modifier = Modifier,
    onNavIconClick: () -> Unit
) {
    AndroChatAppBar(
        modifier = modifier,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = "Tcp Settings",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp
                )
            }
        },
        navIcon = Icons.AutoMirrored.Filled.ArrowBack,
        onNavIconPressed = onNavIconClick
    )
}