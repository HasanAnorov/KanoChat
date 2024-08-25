package com.ierusalem.androchat.features_tcp.tcp_chat.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.features.conversation.presentation.components.messageFormatter
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.ChatMessage

@Composable
fun ChatMessageItem(
    msg: ChatMessage,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    onFileItemClick: (ChatMessage.FileMessage) -> Unit,
    onContactItemClick: (ChatMessage.ContactMessage) -> Unit,
    onPlayVoiceMessageClick: (ChatMessage.VoiceMessage) -> Unit,
    onPauseVoiceMessageClick: (ChatMessage.VoiceMessage) -> Unit,
    onStopVoiceMessageClick: (ChatMessage.VoiceMessage) -> Unit,
) {
    
    val spaceBetweenAuthors = if (isLastMessageByAuthor) Modifier.padding(top = 8.dp) else Modifier
    Row(
        modifier = spaceBetweenAuthors,
        horizontalArrangement = if (msg.isFromYou) Arrangement.End else Arrangement.Start
    ) {
        Spacer(modifier = Modifier.width(24.dp))
        AuthorAndMessage(
            modifier = Modifier
                .padding(end = 16.dp)
                .weight(1f),
            chatMessage = msg,
            isFirstMessageByAuthor = isFirstMessageByAuthor,
            isLastMessageByAuthor = isLastMessageByAuthor,
            onFileItemClick = onFileItemClick,
            onContactItemClick = onContactItemClick,
            onPlayVoiceMessageClick = { onPlayVoiceMessageClick(it) },
            onPauseVoiceMessageClick = { onPauseVoiceMessageClick(it) },
            onStopVoiceMessageClick = { onStopVoiceMessageClick(it) },
        )
    }
}

@Composable
fun AuthorAndMessage(
    modifier: Modifier = Modifier,
    chatMessage: ChatMessage,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    onFileItemClick: (ChatMessage.FileMessage) -> Unit,
    onContactItemClick: (ChatMessage.ContactMessage) -> Unit,
    onPlayVoiceMessageClick: (ChatMessage.VoiceMessage) -> Unit,
    onPauseVoiceMessageClick: (ChatMessage.VoiceMessage) -> Unit,
    onStopVoiceMessageClick: (ChatMessage.VoiceMessage) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (chatMessage.isFromYou) Alignment.End else Alignment.Start
    ) {
        if (isLastMessageByAuthor) {
            AuthorName(isUserMe = chatMessage.isFromYou, peerUserName = chatMessage.peerUsername)
        }
        when (chatMessage) {
            is ChatMessage.TextMessage -> {
                TextMessageItem(message = chatMessage)
            }

            is ChatMessage.VoiceMessage -> {
                VoiceMessageItem(
                    message = chatMessage,
                    onPlayClick = { onPlayVoiceMessageClick(chatMessage) },
                    onPauseClick = { onPauseVoiceMessageClick(chatMessage) },
                    onStopClick = { onStopVoiceMessageClick(chatMessage) },
                )
            }

            is ChatMessage.FileMessage -> {
                FileMessageItem(message = chatMessage, onFileItemClick = onFileItemClick)
            }

            is ChatMessage.ContactMessage -> {
                ContactMessageItem(
                    message = chatMessage,
                    onContactNumberClick = onContactItemClick
                )
            }
        }
        if (isFirstMessageByAuthor) {
            // Last bubble before next author
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            // Between bubbles
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun LocalClickableMessage(
    message: ChatMessage.TextMessage
) {

    val styledMessage = messageFormatter(
        text = message.message,
        primary = message.isFromYou
    )
    Column(
        modifier = Modifier
            .padding(16.dp)
            .width(IntrinsicSize.Max)
    ) {
        ClickableText(
            text = styledMessage,
            style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onBackground),
            onClick = {}
        )
        Text(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(),
            text = message.formattedTime,
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.outline.copy(0.8F),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun AuthorName(
    isUserMe: Boolean,
    peerUserName: String
) {
    Row(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Text(
            text = if (isUserMe) stringResource(id = R.string.author_me) else peerUserName,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .alignBy(LastBaseline)
                .paddingFrom(LastBaseline, after = 8.dp) // Space to 1st bubble
        )
    }
}

@Preview
@Composable
private fun PreviewMessage() {
    AndroChatTheme {
        ChatMessageItem(
            msg = ChatMessage.TextMessage(
                message = ("Hello it is a text"),
                formattedTime = "12:32",
                isFromYou = true,
                messageId = 0L,
                peerUsername = "Khasan"
            ),
            isFirstMessageByAuthor = false,
            isLastMessageByAuthor = true,
            onFileItemClick = {},
            onContactItemClick = {},
            onPlayVoiceMessageClick = {},
            onPauseVoiceMessageClick = {},
            onStopVoiceMessageClick = {}
        )
    }
}

@Preview
@Composable
private fun PreviewMessageDark() {
    AndroChatTheme(isDarkTheme = true) {
        ChatMessageItem(
            msg = ChatMessage.TextMessage(
                message = ("Hello it is a text"),
                formattedTime = "12:32",
                isFromYou = true,
                messageId = 0L,
                peerUsername = "Khasan"
            ),
            isFirstMessageByAuthor = false,
            isLastMessageByAuthor = true,
            onFileItemClick = {},
            onContactItemClick = {},
            onPlayVoiceMessageClick = {},
            onPauseVoiceMessageClick = {},
            onStopVoiceMessageClick = {}
        )
    }
}

@Preview
@Composable
private fun PreviewMessagePeer() {
    AndroChatTheme {
        ChatMessageItem(
            msg = ChatMessage.TextMessage(
                message = ("Hello it is a text"),
                formattedTime = "12:32",
                isFromYou = false,
                messageId = 0L,
                peerUsername = "Khasan"
            ),
            isFirstMessageByAuthor = false,
            isLastMessageByAuthor = true,
            onFileItemClick = {},
            onContactItemClick = {},
            onPlayVoiceMessageClick = {},
            onPauseVoiceMessageClick = {},
            onStopVoiceMessageClick = {}
        )
    }
}

@Preview
@Composable
private fun PreviewMessageDarkPeer() {
    AndroChatTheme(isDarkTheme = true) {
        ChatMessageItem(
            msg = ChatMessage.TextMessage(
                message = ("Hello it is a text"),
                formattedTime = "12:32",
                isFromYou = false,
                messageId = 0L,
                peerUsername = "Khasan"
            ),
            isFirstMessageByAuthor = false,
            isLastMessageByAuthor = true,
            onFileItemClick = {},
            onContactItemClick = {},
            onPlayVoiceMessageClick = {},
            onPauseVoiceMessageClick = {},
            onStopVoiceMessageClick = {}
        )
    }
}

