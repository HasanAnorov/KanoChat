package com.ierusalem.androchat.features_local.tcp_conversation.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.features_local.tcp.domain.model.ChatMessage
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

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
                    val prevAuthor =
                        messages.itemSnapshotList.getOrNull(reversedIndex - 1)?.isFromYou
                    val nextAuthor =
                        messages.itemSnapshotList.getOrNull(reversedIndex + 1)?.isFromYou
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

private val JumpToBottomThreshold = 56.dp
val ChatBubbleShapeStart = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
val ChatBubbleShapeEnd = RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)

@Preview
@Composable
private fun PreviewMessagesContentLight() {
    AndroChatTheme {
        Surface {
            Messages(
                messages = flowOf(
                    PagingData.from(
                        listOf(
                            ChatMessage.TextMessage(
                                formattedTime = "12:02:23",
                                message = "Hello",
                                messageType = AppMessageType.TEXT,
                                isFromYou = true,
                                messageId = 341325321435L,
                                peerUsername = "Hasan"
                            ),
                            ChatMessage.ContactMessage(
                                formattedTime = "12:02:23",
                                contactName = "Hasan",
                                contactNumber = "93 337 36 46",
                                messageType = AppMessageType.TEXT,
                                isFromYou = true,
                                messageId = 341323451435L,
                                peerUsername = "Hasan"
                            ),
                            ChatMessage.TextMessage(
                                formattedTime = "12:02:23",
                                message = "Hello",
                                messageType = AppMessageType.TEXT,
                                isFromYou = true,
                                messageId = 3413251435L,
                                peerUsername = "Hasan"
                            ),
                            ChatMessage.TextMessage(
                                formattedTime = "12:02:23",
                                message = "Hello",
                                messageType = AppMessageType.TEXT,
                                isFromYou = true,
                                messageId = 34132514L,
                                peerUsername = "Hasan"
                            ),
                            ChatMessage.TextMessage(
                                formattedTime = "12:02:23",
                                message = "Hello",
                                messageType = AppMessageType.TEXT,
                                isFromYou = false,
                                messageId = 341325341435L,
                                peerUsername = "Hasan"
                            ),
                            ChatMessage.TextMessage(
                                formattedTime = "12:02:23",
                                message = "Hello",
                                messageType = AppMessageType.TEXT,
                                isFromYou = true,
                                messageId = 34132521231435L,
                                peerUsername = "Hasan"
                            ),
                            ChatMessage.TextMessage(
                                formattedTime = "12:02:23",
                                message = "Hello",
                                messageType = AppMessageType.TEXT,
                                isFromYou = true,
                                messageId = 341327451435L,
                                peerUsername = "Hasan"
                            ),
                            ChatMessage.TextMessage(
                                formattedTime = "12:02:23",
                                message = "Hello",
                                messageType = AppMessageType.TEXT,
                                isFromYou = false,
                                messageId = 341326651435L,
                                peerUsername = "Hasan"
                            ),
                            ChatMessage.TextMessage(
                                formattedTime = "12:02:23",
                                message = "Hello",
                                messageType = AppMessageType.TEXT,
                                isFromYou = false,
                                messageId = 341325142134635L,
                                peerUsername = "Hasan"
                            ),
                            ChatMessage.TextMessage(
                                formattedTime = "12:02:23",
                                message = "Hello",
                                messageType = AppMessageType.TEXT,
                                isFromYou = true,
                                messageId = 3413255531435L,
                                peerUsername = "Hasan"
                            ), ChatMessage.TextMessage(
                                formattedTime = "12:02:23",
                                message = "Hello",
                                messageType = AppMessageType.TEXT,
                                isFromYou = true,
                                messageId = 3413251454335L,
                                peerUsername = "Hasan"
                            ),
                            ChatMessage.TextMessage(
                                formattedTime = "12:02:23",
                                message = "Hello",
                                messageType = AppMessageType.TEXT,
                                isFromYou = false,
                                messageId = 341322345551435L,
                                peerUsername = "Hasan"
                            ),
                            ChatMessage.TextMessage(
                                formattedTime = "12:02:23",
                                message = "Hello",
                                messageType = AppMessageType.TEXT,
                                isFromYou = true,
                                messageId = 341321551435L,
                                peerUsername = "Hasan"
                            )
                        )
                    )
                ).collectAsLazyPagingItems(),
                scrollState = rememberLazyListState(),
                onFileItemClicked = {},
                onContactItemClick = {},
                onPlayVoiceMessageClick = {},
                onPauseVoiceMessageClick = {},
                onStopVoiceMessageClick = {}
            )
        }
    }
}