package com.ierusalem.androchat.features_local.tcp_conversation.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.features_local.tcp.domain.model.ChatMessage

@Composable
fun ChatMessageItem(
    msg: ChatMessage,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    onFileItemClick: (ChatMessage.FileMessage) -> Unit,
    onSaveToDownloadsClick: (ChatMessage.FileMessage) -> Unit = {},
    onContactItemClick: (ChatMessage.ContactMessage) -> Unit,
    onPlayVoiceMessageClick: (ChatMessage.VoiceMessage) -> Unit,
    onResumeVoiceMessageClick: (ChatMessage.VoiceMessage) -> Unit = {},
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
            onFileItemClick = onFileItemClick,
            onContactItemClick = onContactItemClick,
            onPlayVoiceMessageClick = { onPlayVoiceMessageClick(it) },
            onResumeVoiceMessageClick = {onResumeVoiceMessageClick(it)},
            onPauseVoiceMessageClick = { onPauseVoiceMessageClick(it) },
            onStopVoiceMessageClick = { onStopVoiceMessageClick(it) },
            onSaveToDownloadsClick = onSaveToDownloadsClick
        )
    }
}

@Composable
fun AuthorAndMessage(
    modifier: Modifier = Modifier,
    chatMessage: ChatMessage,
    isFirstMessageByAuthor: Boolean,
    onFileItemClick: (ChatMessage.FileMessage) -> Unit,
    onSaveToDownloadsClick: (ChatMessage.FileMessage) -> Unit,
    onContactItemClick: (ChatMessage.ContactMessage) -> Unit,
    onPlayVoiceMessageClick: (ChatMessage.VoiceMessage) -> Unit,
    onResumeVoiceMessageClick: (ChatMessage.VoiceMessage) -> Unit,
    onPauseVoiceMessageClick: (ChatMessage.VoiceMessage) -> Unit,
    onStopVoiceMessageClick: (ChatMessage.VoiceMessage) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (chatMessage.isFromYou) Alignment.End else Alignment.Start
    ) {
        when (chatMessage) {
            is ChatMessage.TextMessage -> {
                TextMessageItem(message = chatMessage)
            }

            is ChatMessage.VoiceMessage -> {
                VoiceMessageItem(
                    message = chatMessage,
                    onPlayClick = { onPlayVoiceMessageClick(chatMessage) },
                    onPauseClick = { onPauseVoiceMessageClick(chatMessage) },
                    onResumeClick = { onResumeVoiceMessageClick(chatMessage) },
                    onStopClick = { onStopVoiceMessageClick(chatMessage) },
                )
            }

            is ChatMessage.FileMessage -> {
                FileMessageItem(
                    message = chatMessage,
                    onFileItemClick = onFileItemClick,
                    onSaveToDownloadsClick = onSaveToDownloadsClick
                )
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

