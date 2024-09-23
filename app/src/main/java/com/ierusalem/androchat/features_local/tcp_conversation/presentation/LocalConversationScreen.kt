package com.ierusalem.androchat.features_local.tcp_conversation.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.core.ui.components.AndroChatAppBar
import com.ierusalem.androchat.core.ui.components.ErrorScreen
import com.ierusalem.androchat.core.ui.components.ErrorType
import com.ierusalem.androchat.core.ui.components.LoadingScreen
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.Resource
import com.ierusalem.androchat.features_local.tcp.domain.model.ChatMessage
import com.ierusalem.androchat.features_local.tcp.domain.model.ChattingUser
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenUiState
import com.ierusalem.androchat.features_local.tcp.presentation.TcpScreenEvents
import com.ierusalem.androchat.features_local.tcp.presentation.components.NetworkErrorDialog
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
    uiState: TcpScreenUiState,
    modifier: Modifier = Modifier,
    eventHandler: (TcpScreenEvents) -> Unit,
) {

    val scrollState = rememberLazyListState()
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            when (uiState.currentChattingUser) {
                is Resource.Loading -> {
                    ConversationChannelBar(
                        contact = null,
                        onNavIconPressed = { eventHandler(TcpScreenEvents.OnNavIconClick) },
                        scrollBehavior = scrollBehavior,
                    )
                }

                is Resource.Failure -> {
                    ConversationChannelBar(
                        contact = null,
                        onNavIconPressed = { eventHandler(TcpScreenEvents.OnNavIconClick) },
                        scrollBehavior = scrollBehavior,
                    )
                }

                is Resource.Success -> {
                    ConversationChannelBar(
                        contact = uiState.currentChattingUser.data,
                        onNavIconPressed = { eventHandler(TcpScreenEvents.OnNavIconClick) },
                        scrollBehavior = scrollBehavior,
                    )
                }
            }
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
                when (uiState.currentChattingUser) {
                    is Resource.Loading -> {
                        LoadingScreen(modifier = Modifier.weight(1F), isForChat = true)
                    }

                    is Resource.Failure -> {
                        ErrorScreen(
                            modifier = Modifier.weight(1F),
                            error = ErrorType.InvalidResponse
                        )
                    }

                    is Resource.Success -> {
                        Messages(
                            messages = uiState.messages.collectAsLazyPagingItems(),
                            modifier = Modifier.weight(1f),
                            scrollState = scrollState,
                            onFileItemClicked = { eventHandler(TcpScreenEvents.OnFileItemClick(it)) },
                            onSaveToDownloadsClick = {
                                eventHandler(TcpScreenEvents.OnSaveToDownloadsClick(it))
                            },
                            onContactItemClick = {
                                eventHandler(
                                    TcpScreenEvents.OnContactItemClick(
                                        it
                                    )
                                )
                            },
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
                    }
                }
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
    contact: ChattingUser?,
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
                contact?.let {
                    Text(
                        text = contact.username,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                // Number of members
                contact?.let {
                    if (it.isOnline) {
                        Text(
                            text = stringResource(id = R.string.online),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF35C47C)
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.offline),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        navIcon = Icons.AutoMirrored.Filled.ArrowBack,
        actions = {}
    )
}

@Preview
@Composable
private fun PreviewLocalConversation() {
    AndroChatTheme {
        ConversationContent(
            uiState = TcpScreenUiState(
                currentChattingUser = Resource.Loading(),
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

@Preview
@Composable
private fun PreviewLocalConversationDark() {
    AndroChatTheme(isDarkTheme = true) {
        ConversationContent(
            uiState = TcpScreenUiState(
                currentChattingUser = Resource.Success(
                    ChattingUser(
                        userUniqueId = "123",
                        username = "Ahmed",
                        isOnline = false,
                        avatarBackgroundColor = 0xFF5733,
                        lastMessage = null
                    )
                ),
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


@Preview
@Composable
private fun PreviewLocalConversationDarkFailure() {
    AndroChatTheme(isDarkTheme = true) {
        ConversationContent(
            uiState = TcpScreenUiState(
                currentChattingUser = Resource.Failure("Something went wrong"),
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