package com.ierusalem.androchat.features_tcp.tcp_chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.components.FunctionalityNotAvailablePopup
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.features.conversation.presentation.components.JumpToBottom
import com.ierusalem.androchat.features_tcp.tcp.domain.state.TcpScreenUiState
import com.ierusalem.androchat.features_tcp.tcp.presentation.utils.TcpScreenEvents
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.ChatMessage
import com.ierusalem.androchat.features_tcp.tcp_chat.presentation.components.FileMessageItem
import kotlinx.coroutines.launch

@Composable
fun LocalConversationContent(
    modifier: Modifier = Modifier,
    uiState: TcpScreenUiState,
    eventHandler: (TcpScreenEvents) -> Unit,
) {
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.surfaceDim.copy(0.2F)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceDim.copy(0.4F)),
                contentAlignment = Alignment.Center
            ) {
                ChannelNameBar(
                    channelName = uiState.peerUniqueName,
                    isOnline = true,
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.background,
                thickness = 1.dp
            )
        }
        Column(
            modifier = Modifier
                .weight(1F)
                .background(MaterialTheme.colorScheme.surfaceDim.copy(0.12F)),
            content = {
                Messages(
                    messages = uiState.messages.reversed(),
                    modifier = Modifier.weight(1f),
                    scrollState = scrollState,
                    onFileItemClicked = { eventHandler(TcpScreenEvents.OnFileItemClick(it)) },
                    onContactItemClick = { eventHandler(TcpScreenEvents.OnContactItemClick(it)) },
                    onPlayVoiceMessageClick = {
                        eventHandler(
                            TcpScreenEvents.OnPlayVoiceMessageClick(
                                it
                            )
                        )
                    },
                    onPauseVoiceMessageClick = {
                        eventHandler(
                            TcpScreenEvents.OnPauseVoiceMessageClick(
                                it
                            )
                        )
                    },
                    onStopVoiceMessageClick = {
                        eventHandler(
                            TcpScreenEvents.OnStopVoiceMessageClick(
                                it
                            )
                        )
                    },
                )
                LocalConversationUserInput(
                    // let this element handle the padding so that the elevation is shown behind the
                    // navigation bar
                    modifier = Modifier
                        .navigationBarsPadding()
                        .imePadding(),
                    uiState = uiState,
                    eventHandler = eventHandler,
                    resetScroll = {
                        scope.launch {
                            scrollState.scrollToItem(0)
                        }
                    },
                )
            }
        )
    }

}

@Composable
fun ChannelNameBar(
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
                text = if(isOnline) stringResource(id = R.string.connected) else stringResource(id = R.string.not_connected),
                style = MaterialTheme.typography.bodySmall,
                color = if(isOnline) Color(0xFF35C47C) else Color.Red
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

const val ConversationTestTag = "ConversationTestTag"

@Composable
fun Messages(
    messages: List<ChatMessage>,
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    onFileItemClicked: (ChatMessage.FileMessage) -> Unit,
    onContactItemClick: (ChatMessage.ContactMessage) -> Unit,
    onPlayVoiceMessageClick: (ChatMessage.VoiceMessage) -> Unit,
    onPauseVoiceMessageClick: (ChatMessage.VoiceMessage) -> Unit,
    onStopVoiceMessageClick: (ChatMessage.VoiceMessage) -> Unit,
) {
    val scope = rememberCoroutineScope()
    Box(modifier = modifier) {

        LazyColumn(
            reverseLayout = true,
            state = scrollState,
            modifier = Modifier
                .testTag(ConversationTestTag)
                .fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
            itemsIndexed(messages) { index, message ->
                /// todo: fix this
                val prevAuthor = messages.getOrNull(index - 1)?.isFromYou
                val nextAuthor = messages.getOrNull(index + 1)?.isFromYou
                val isFirstMessageByAuthor = prevAuthor != message.isFromYou
                val isLastMessageByAuthor = nextAuthor != message.isFromYou
                ChatMessageItem(
                    msg = message,
                    isFirstMessageByAuthor = isFirstMessageByAuthor,
                    isLastMessageByAuthor = isLastMessageByAuthor,
                    onFileItemClick = onFileItemClicked,
                    onContactItemClick = onContactItemClick,
                    onPlayVoiceMessageClick = {onPlayVoiceMessageClick(it)},
                    onPauseVoiceMessageClick = {onPauseVoiceMessageClick(it)},
                    onStopVoiceMessageClick = {onStopVoiceMessageClick(it)},
                )
            }
        }
        // Jump to bottom button shows up when user scrolls past a threshold.
        // Convert to pixels:
        val jumpThreshold = with(LocalDensity.current) {
            JumpToBottomThreshold.toPx()
        }

        // Show the button if the first visible item is not the first one or if the offset is
        // greater than the threshold.
        val jumpToBottomButtonEnabled by remember {
            derivedStateOf {
                scrollState.firstVisibleItemIndex != 0 ||
                        scrollState.firstVisibleItemScrollOffset > jumpThreshold
            }
        }

        JumpToBottom(
            // Only show if the scroller is not at the bottom
            enabled = jumpToBottomButtonEnabled,
            onClicked = {
                scope.launch {
                    scrollState.animateScrollToItem(0)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

val ChatBubbleShapeStart = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
val ChatBubbleShapeEnd = RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)

@Composable
fun ChatMessageItem(
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
        ChatMessageItem(
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
        ChatMessageItem(
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
        ChatMessageItem(
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
        ChatMessageItem(
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
private fun PreviewLightFileItem() {
    AndroChatTheme {
        FileMessageItem(
            modifier = Modifier,
            message = ChatMessage.FileMessage(
                formattedTime = "12:12:12, jul 12 2034",
                fileName = "SamsungElectronics Dubai Global Version home.edition.com",
                fileSize = "16 Kb",
                fileExtension = ".pdf",
                filePath = "file_path_uri",
                isFromYou = true,
                messageId = 0L,
                peerUsername = "Khasan"
            ),
            onFileItemClick = {}
        )
    }
}

@Preview
@Composable
private fun PreviewDarkFileItem() {
    AndroChatTheme(isDarkTheme = true) {
        FileMessageItem(
            modifier = Modifier,
            message = ChatMessage.FileMessage(
                formattedTime = "12:12:12, jul 12 2034",
                fileName = "SamsungElectronics Dubai Global Version home.edition.com",
                fileSize = "16 Kb",
                fileExtension = ".pdf",
                filePath = "file_path_uri",
                isFromYou = true,
                messageId = 0L,
                peerUsername = "Khasan"
            ),
            onFileItemClick = {}
        )
    }
}

@Preview
@Composable
fun ConversationPreview() {
    AndroChatTheme {
        LocalConversationContent(
            uiState = TcpScreenUiState(),
            eventHandler = {}
        )
    }
}

@Preview
@Composable
fun ChannelBarPrev() {
    AndroChatTheme {
        Surface {
            ChannelNameBar(
                channelName = "composers",
                isOnline = true
            )
        }
    }
}

private val JumpToBottomThreshold = 56.dp
