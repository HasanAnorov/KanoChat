package com.ierusalem.androchat.features_local.tcp_chat.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.features_local.tcp_chat.data.db.entity.ChatMessage
import com.ierusalem.androchat.features_local.tcp_chat.presentation.ChatBubbleShapeEnd
import com.ierusalem.androchat.features_local.tcp_chat.presentation.ChatBubbleShapeStart

@Composable
fun TextMessageItem(
    message: ChatMessage.TextMessage
) {
    val backgroundBubbleColor = if (message.isFromYou) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.inverseOnSurface
    }
    Surface(
        color = backgroundBubbleColor,
        shape = if (message.isFromYou) ChatBubbleShapeEnd else ChatBubbleShapeStart,
    ) {
        LocalClickableMessage(message = message)
    }
}

@Preview
@Composable
private fun PreviewLocalChatItemBubble() {
    AndroChatTheme {
        TextMessageItem(
            message = ChatMessage.TextMessage(
                formattedTime = "12:12:12, jul 12 2034",
                message = "Assalom alekum aka yaxshimisiz",
                isFromYou = true,
                messageId = 0L,
                peerUsername = "Khasan"
            )
        )
    }
}

@Preview
@Composable
private fun PreviewLocalChatItemBubblePeer() {
    AndroChatTheme {
        TextMessageItem(
            message = ChatMessage.TextMessage(
                formattedTime = "12:12:12, jul 12 2034",
                message = "Assalom alekum aka yaxshimisiz",
                isFromYou = false,
                messageId = 0L,
                peerUsername = "Khasan"
            )
        )
    }
}

@Preview
@Composable
private fun PreviewLocalChatItemBubbleDark() {
    AndroChatTheme(isDarkTheme = true) {
        TextMessageItem(
            message = ChatMessage.TextMessage(
                formattedTime = "12:12:12, jul 12 2034",
                message = "Assalom alekum aka yaxshimisiz",
                isFromYou = true,
                messageId = 0L,
                peerUsername = "Khasan"
            )
        )
    }
}

@Preview
@Composable
private fun PreviewLocalChatItemBubbleDarkPeer() {
    AndroChatTheme(isDarkTheme = true) {
        TextMessageItem(
            message = ChatMessage.TextMessage(
                formattedTime = "12:12:12, jul 12 2034",
                message = "Assalom alekum aka yaxshimisiz",
                isFromYou = false,
                messageId = 0L,
                peerUsername = "Khasan"
            )
        )
    }
}