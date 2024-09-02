package com.ierusalem.androchat.features_local.tcp_conversation.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.components.FunctionalityNotAvailablePopup
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.features_local.tcp.domain.model.ChatMessage
import com.ierusalem.androchat.features_local.tcp_conversation.presentation.components.ChatMessageItem
import com.ierusalem.androchat.features_remote.conversation.presentation.components.JumpToBottom
import kotlinx.coroutines.launch

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

const val ConversationTestTag = "ConversationTestTag"

@Composable
fun Messages(
    messages: LazyPagingItems<ChatMessage>,
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

            items(
                count = messages.itemCount,
                key = messages.itemKey { chatMessage -> chatMessage.messageId },
                contentType = messages.itemContentType { "ChatMessages" }
            ) { index: Int ->
                // Get the reversed index
                val reversedIndex = messages.itemCount - 1 - index
                val chatMessage: ChatMessage? = messages[reversedIndex]

                chatMessage?.let {
                    // todo: fix this
                    val prevAuthor = messages.itemSnapshotList.getOrNull(reversedIndex - 1)?.isFromYou
                    val nextAuthor = messages.itemSnapshotList.getOrNull(reversedIndex + 1)?.isFromYou
                    val isFirstMessageByAuthor = prevAuthor != chatMessage.isFromYou
                    val isLastMessageByAuthor = nextAuthor != chatMessage.isFromYou
                    ChatMessageItem(
                        msg = chatMessage,
                        isFirstMessageByAuthor = isFirstMessageByAuthor,
                        isLastMessageByAuthor = isLastMessageByAuthor,
                        onFileItemClick = onFileItemClicked,
                        onContactItemClick = onContactItemClick,
                        onPlayVoiceMessageClick = { onPlayVoiceMessageClick(it) },
                        onPauseVoiceMessageClick = { onPauseVoiceMessageClick(it) },
                        onStopVoiceMessageClick = { onStopVoiceMessageClick(it) },
                    )
                }
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
