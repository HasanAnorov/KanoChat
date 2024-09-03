package com.ierusalem.androchat.features_local.tcp_conversation.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.core.ui.components.AndroChatAppBar
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenUiState
import com.ierusalem.androchat.features_local.tcp.presentation.components.NetworkErrorDialog
import com.ierusalem.androchat.features_local.tcp.presentation.TcpScreenEvents
import com.ierusalem.androchat.features_local.tcp.domain.model.ChatMessage
import com.ierusalem.androchat.features_local.tcp_conversation.presentation.components.LocalConversationUserInput
import com.ierusalem.androchat.features_local.tcp_conversation.presentation.components.Messages
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * Entry point for a conversation screen.
 *
 * @param uiState [TcpScreenUiState] that contains messages to display
 * @param modifier [Modifier] to apply to this layout node
 * @param eventHandler [TcpScreenEvents] to handle user clicks
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationContent(
    modifier: Modifier = Modifier,
    uiState: TcpScreenUiState,
    eventHandler: (TcpScreenEvents) -> Unit,
) {

    val scrollState = rememberLazyListState()
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            val channelNameBar = uiState.currentChattingUser?.userUniqueName ?: "Unknown"
            ConversationChannelBar(
                channelName = channelNameBar,
                channelMembers = "Online",
                onNavIconPressed = { eventHandler(TcpScreenEvents.OnNavIconClick) },
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        if (uiState.hasDialogErrorOccurred != null) {
            NetworkErrorDialog(
                onDismissRequest = { eventHandler(TcpScreenEvents.OnDialogErrorOccurred(null)) },
                onConfirmation = { eventHandler(TcpScreenEvents.OnDialogErrorOccurred(null)) },
                dialogTitle = uiState.hasDialogErrorOccurred.title,
                dialogText = uiState.hasDialogErrorOccurred.message,
                icon = painterResource(id = uiState.hasDialogErrorOccurred.icon)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            content = {
                Messages(
                    messages = uiState.messages.collectAsLazyPagingItems(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationChannelBar(
    channelName: String,
    channelMembers: String,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavIconPressed: () -> Unit = { }
) {
    AndroChatAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        onNavIconPressed = onNavIconPressed,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Channel name
                Text(
                    text = channelName,
                    style = MaterialTheme.typography.titleMedium
                )
                // Number of members
                Text(
                    text = channelMembers,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navIcon = Icons.AutoMirrored.Filled.ArrowBack,
        actions = {
            // Search icon
            Icon(
                imageVector = Icons.Outlined.Search,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clickable(onClick = { })
                    .padding(horizontal = 12.dp, vertical = 16.dp)
                    .height(24.dp),
                contentDescription = stringResource(id = R.string.search)
            )
        }
    )
}

@Preview
@Composable
private fun PreviewLocalConversation() {
    AndroChatTheme {
        ConversationContent(uiState = TcpScreenUiState(), eventHandler = {})
    }
}

@Preview
@Composable
private fun PreviewLocalConversationDark() {
    AndroChatTheme(isDarkTheme = true) {
        ConversationContent(
            uiState = TcpScreenUiState(
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
                            ChatMessage.TextMessage(
                                formattedTime = "12:02:23",
                                message = "Hello",
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
                )
            ),
            eventHandler = {}
        )
    }
}
