package com.ierusalem.androchat.features_local.tcp_conversation.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.features_local.tcp.domain.state.ContactItem

@Composable
fun ContactItemContent(
    modifier: Modifier = Modifier,
    contactItem: ContactItem,
    onItemCLick: () -> Unit ,
) {
    Row(
        modifier = modifier.fillMaxWidth().clickable { onItemCLick() },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1F)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                text = contactItem.contactName,
                style = MaterialTheme.typography.titleMedium

            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = contactItem.phoneNumber,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (contactItem.isSelected) {
            Icon(
                modifier = Modifier.padding(end = 16.dp),
                painter = painterResource(id = R.drawable.check_circle),
                contentDescription = null,
                tint = Color(0xFF35C47C)
            )
        }
    }
}

@Preview
@Composable
private fun PreviewContactItemContent() {
    AndroChatTheme {
        Surface {
            ContactItemContent(
                contactItem = ContactItem("Hasan", "93 3373646", true),
                onItemCLick = {}
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDarkContactItemContent() {
    AndroChatTheme(isDarkTheme = true) {
        Surface {
            ContactItemContent(
                contactItem = ContactItem("Hasan", "93 3373646", false),
                onItemCLick = {}
            )
        }
    }
}