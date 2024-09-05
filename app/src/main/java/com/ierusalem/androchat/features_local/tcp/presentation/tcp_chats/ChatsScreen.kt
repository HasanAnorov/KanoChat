package com.ierusalem.androchat.features_local.tcp.presentation.tcp_chats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.core.ui.components.ErrorScreen
import com.ierusalem.androchat.core.ui.components.LoadingScreen
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.RandomColors
import com.ierusalem.androchat.core.utils.Resource
import com.ierusalem.androchat.features_local.tcp.domain.model.ChattingUser
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenUiState
import com.ierusalem.androchat.features_local.tcp.presentation.TcpScreenEvents
import com.ierusalem.androchat.features_local.tcp.presentation.tcp_chats.components.NoMessagesScreen
import com.ierusalem.androchat.features_local.tcp.presentation.tcp_chats.components.TcpContactItem
import com.ierusalem.androchat.features_remote.home.presentation.contacts.ErrorType

@Composable
fun ChatsScreen(
    modifier: Modifier = Modifier,
    uiState: TcpScreenUiState,
    onCreateNetworkClick: () -> Unit = {},
    eventHandler: (TcpScreenEvents) -> Unit
) {
    when (val state = uiState.chattingUsers) {
        is Resource.Loading -> LoadingScreen(modifier)

        is Resource.Success -> {

            val users = state.data ?: emptyList()

            // open navigation drawer swiper from left to right on initial page
            val isVisible = rememberSaveable { mutableStateOf(true) }

            val nestedScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        // Hide FAB
                        if (available.y < -1) {
                            isVisible.value = false
                        }

                        // Show FAB
                        if (available.y > 1) {
                            isVisible.value = true
                        }

                        return Offset.Zero
                    }
                }
            }

            Box(modifier = modifier) {
                if (users.isEmpty()) {
                    NoMessagesScreen(onCreateNetworkClick = onCreateNetworkClick)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxSize()
                            .nestedScroll(nestedScrollConnection),
                        verticalArrangement = Arrangement.Top,
                        content = {
                            itemsIndexed(items = users) { index, contact ->

                                TcpContactItem(
                                    contact = contact,
                                    modifier = Modifier,
                                    onClick = {
                                        eventHandler(TcpScreenEvents.TcpChatItemClicked(contact))
                                    },
                                    lastMessage = contact.lastMessage
                                )
                                if (index < users.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 66.dp, end = 12.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(0.4F)
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }

        is Resource.Failure -> {
            ErrorScreen(error = ErrorType.InvalidResponse, onRetryClick = {})
        }
    }

}

@Preview
@Composable
fun ContactsScreenPreview() {
    AndroChatTheme {
        ChatsScreen(
            modifier = Modifier,
            eventHandler = {},
            uiState = TcpScreenUiState(
                contacts = Resource.Failure("Something went wrong")
            )
        )
    }
}

@Preview
@Composable
fun ContactsScreenPreviewSuccess() {
    AndroChatTheme {
        ChatsScreen(
            modifier = Modifier,
            eventHandler = {},
            uiState = TcpScreenUiState(
                chattingUsers = Resource.Success(
                    listOf(
                        ChattingUser(
                            userUniqueId = "123",
                            username = "Ahmed",
                            isOnline = true,
                            avatarBackgroundColor = RandomColors().getColor(),
                            lastMessage = null
                        ),
                        ChattingUser(
                            userUniqueId = "123",
                            username = "Ahmed",
                            isOnline = true,
                            avatarBackgroundColor = RandomColors().getColor(),
                            lastMessage = null
                        ),
                        ChattingUser(
                            userUniqueId = "123",
                            username = "Ahmed",
                            isOnline = false,
                            avatarBackgroundColor = RandomColors().getColor(),
                            lastMessage = null
                        ),
                        ChattingUser(
                            userUniqueId = "123",
                            username = "Ahmed",
                            isOnline = false,
                            avatarBackgroundColor = RandomColors().getColor(),
                            lastMessage = null
                        ),
                        ChattingUser(
                            userUniqueId = "123",
                            username = "Ahmed",
                            isOnline = false,
                            avatarBackgroundColor = RandomColors().getColor(),
                            lastMessage = null
                        ),
                        ChattingUser(
                            userUniqueId = "123",
                            username = "Ahmed",
                            isOnline = false,
                            avatarBackgroundColor = RandomColors().getColor(),
                            lastMessage = null
                        ),
                        ChattingUser(
                            userUniqueId = "123",
                            username = "Ahmed",
                            isOnline = false,
                            avatarBackgroundColor = RandomColors().getColor(),
                            lastMessage = null
                        ),
                        ChattingUser(
                            userUniqueId = "123",
                            username = "Ahmed",
                            isOnline = false,
                            avatarBackgroundColor = RandomColors().getColor(),
                            lastMessage = null
                        ),
                        ChattingUser(
                            userUniqueId = "123",
                            username = "Ahmed",
                            isOnline = false,
                            avatarBackgroundColor = RandomColors().getColor(),
                            lastMessage = null
                        ),
                        ChattingUser(
                            userUniqueId = "123",
                            username = "Ahmed",
                            isOnline = false,
                            avatarBackgroundColor = RandomColors().getColor(),
                            lastMessage = null
                        )
                    )
                )
            )
        )
    }
}

@Preview
@Composable
fun ContactsScreenPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        ChatsScreen(
            modifier = Modifier,
            eventHandler = {},
            uiState = TcpScreenUiState(
                chattingUsers = Resource.Success(listOf())
            )
        )
    }
}
