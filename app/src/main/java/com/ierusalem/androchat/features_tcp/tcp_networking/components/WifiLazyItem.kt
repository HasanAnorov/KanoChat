package com.ierusalem.androchat.features_tcp.tcp_networking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme

@Composable
fun WifiLazyItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    wifiName: String
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1F)
                .padding(vertical = 16.dp)
                .padding(start = 8.dp, end = 8.dp),
            color = MaterialTheme.colorScheme.onBackground,
            text = wifiName,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            modifier = Modifier.padding(end = 16.dp),
            imageVector = Icons.Default.Wifi,
            contentDescription = null
        )
    }
}

@Preview
@Composable
private fun PreviewWifiLazyItem() {
    AndroChatTheme(isDarkTheme = false) {
        Surface {
            WifiLazyItem(
                onClick = {},
                wifiName = "Example Wifi"
            )
        }
    }
}