package com.ierusalem.androchat.features_local.tcp_conversation.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.components.FunctionalityNotAvailablePopup
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme

@Composable
fun ConversationTopBar(
    channelName: String,
    isOnline: Boolean,
    modifier: Modifier = Modifier,
) {
    var functionalityNotAvailablePopupShown by remember { mutableStateOf(false) }
    if (functionalityNotAvailablePopupShown) {
        FunctionalityNotAvailablePopup { functionalityNotAvailablePopupShown = false }
    }
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1F),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = channelName,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = if (isOnline) stringResource(id = R.string.connected) else stringResource(id = R.string.not_connected),
                style = MaterialTheme.typography.bodySmall,
                color = if (isOnline) Color(0xFF35C47C) else Color.Red
            )
        }
        // Search icon
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Outlined.Search,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                contentDescription = stringResource(id = R.string.search)
            )
        }
    }
}


@Preview
@Composable
fun ChannelBarPrev() {
    AndroChatTheme {
        Surface {
            ConversationTopBar(
                channelName = "composers",
                isOnline = true
            )
        }
    }
}

