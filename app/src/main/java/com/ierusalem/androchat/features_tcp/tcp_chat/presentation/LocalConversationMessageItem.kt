package com.ierusalem.androchat.features_tcp.tcp_chat.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.features.auth.register.domain.model.Message
import com.ierusalem.androchat.features.conversation.presentation.components.messageFormatter
import com.ierusalem.androchat.features_tcp.tcp_chat.presentation.components.FileMessageItem

@Composable
fun LocalMessageItem(
    msg: Message,
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    onFileItemClick: (Message.FileMessage) -> Unit
) {
    val borderColor = if (isUserMe) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    val spaceBetweenAuthors = if (isLastMessageByAuthor) Modifier.padding(top = 8.dp) else Modifier
    Row(modifier = spaceBetweenAuthors) {
        if (isLastMessageByAuthor) {
            // Avatar
            Image(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(42.dp)
                    .border(1.5.dp, borderColor, CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .clip(CircleShape)
                    .align(Alignment.Top),
                painter = painterResource(id = R.drawable.be_doer),
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
        } else {
            // Space under avatar
            Spacer(modifier = Modifier.width(74.dp))
        }
        AuthorAndTextMessage(
            modifier = Modifier
                .padding(end = 16.dp)
                .weight(1f),
            msg = msg,
            isUserMe = isUserMe,
            isFirstMessageByAuthor = isFirstMessageByAuthor,
            isLastMessageByAuthor = isLastMessageByAuthor,
            onFileItemClick = onFileItemClick
        )
    }
}

@Composable
fun AuthorAndTextMessage(
    modifier: Modifier = Modifier,
    msg: Message,
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    onFileItemClick: (Message.FileMessage) -> Unit
) {
    Column(modifier = modifier) {
        if (isLastMessageByAuthor) {
            AuthorName(msg, isUserMe)
        }
        when (msg) {
            is Message.TextMessage -> {
                LocalMessageItem(msg, isUserMe)
            }

            is Message.FileMessage -> {
                FileMessageItem(message = msg, onFileItemClick = onFileItemClick)
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
    message: Message.TextMessage,
    isUserMe: Boolean,
) {

    val styledMessage = messageFormatter(
        text = message.message,
        primary = isUserMe
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
            color = MaterialTheme.colorScheme.onBackground.copy(0.8F),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun AuthorName(
    message: Message,
    isUserMe: Boolean
) {
    // Combine author and timestamp for a11y.
    Row(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Text(
            text = if (isUserMe) stringResource(id = R.string.author_me) else message.username,
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
        LocalMessageItem(
            msg = Message.TextMessage(
                message = ("Hello it is a text"),
                formattedTime = "12:32",
                username = "Owner",
                isFromYou = true
            ),
            isFirstMessageByAuthor = false,
            isLastMessageByAuthor = true,
            isUserMe = true,
            onFileItemClick = {}
        )
    }
}

