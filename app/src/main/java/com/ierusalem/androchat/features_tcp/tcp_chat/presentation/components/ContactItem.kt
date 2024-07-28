package com.ierusalem.androchat.features_tcp.tcp_chat.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.ChatMessage
import com.ierusalem.androchat.features.conversation.presentation.components.messageFormatter

@Composable
fun ContactItem(
    modifier: Modifier = Modifier,
    message: ChatMessage.ContactMessage,
    onContactNumberClick:(ChatMessage.ContactMessage) -> Unit
) {
    val backgroundBubbleColor = if (message.isFromYou) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contactNumber = messageFormatter(
        text = message.contactNumber,
        primary = message.isFromYou
    )
    Surface(
        modifier = modifier,
        color = backgroundBubbleColor,
        shape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(IntrinsicSize.Max)
        ) {
            Row {
                Text(
                    modifier = Modifier
                        .padding(top = 4.dp),
                    text = "[Name]",
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    modifier = Modifier
                        .padding(top = 4.dp, start = 4.dp),
                    text = message.contactName,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Row(horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier
                        .padding(top = 4.dp),
                    text = "[Number]",
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                    style = MaterialTheme.typography.labelSmall
                )
                ClickableText(
                    modifier = Modifier
                        .padding(top = 4.dp, start = 4.dp),
                    text = contactNumber,
                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onPrimary, textAlign = TextAlign.End,),
                    onClick = {
                        onContactNumberClick(message)
                    }
                )
            }
            Text(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                text = message.formattedTime,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Preview
@Composable
private fun PreviewLightContactItem() {
    AndroChatTheme {
        ContactItem(
            message = ChatMessage.ContactMessage(
                formattedTime = "12:12:12, jul 12 2034",
                contactNumber = "93 337 36 46",
                contactName = "Anorov Hasan",
                isFromYou = true,
                messageId = 0L
            ),
            onContactNumberClick = {}
        )
    }
}

@Preview
@Composable
private fun PreviewDarkContactItem() {
    AndroChatTheme(isDarkTheme = true) {
        ContactItem(
            message = ChatMessage.ContactMessage(
                formattedTime = "12:12:12, jul 12 2034",
                contactNumber = "93 337 36 46",
                contactName = "Anorov Hasan",
                isFromYou = true,
                messageId = 0L
            ),
            onContactNumberClick = {}
        )
    }
}