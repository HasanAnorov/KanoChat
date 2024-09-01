package com.ierusalem.androchat.features_local.tcp_chats

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.core.ui.components.ErrorScreen
import com.ierusalem.androchat.core.ui.components.FabButton
import com.ierusalem.androchat.core.ui.components.LoadingScreen
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.Resource
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChattingUserEntity
import com.ierusalem.androchat.features_local.tcp.domain.InitialChatModel
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenUiState
import com.ierusalem.androchat.features_local.tcp.presentation.utils.TcpScreenEvents
import com.ierusalem.androchat.features_remote.home.presentation.contacts.ErrorType

@Composable
fun ChatsScreen(
    modifier: Modifier = Modifier,
    uiState: TcpScreenUiState,
    eventHandler: (TcpScreenEvents) -> Unit
) {
    when (uiState.contactsList) {
        is Resource.Loading -> LoadingScreen(modifier)

        is Resource.Success -> {
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

            val data = uiState.contactsList.data!!
            Box(modifier = modifier) {
                if (data.isEmpty()) {
                    Log.d("ahi3646", "ContactsScreen: Data is empty ")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxSize()
                            .nestedScroll(nestedScrollConnection),
                        verticalArrangement = Arrangement.Top,
                        content = {
                            itemsIndexed(data) { index, contact ->
                                TcpContactItem(
                                    contact = contact,
                                    modifier = Modifier,
                                    onClick = {
                                        val selectedChattingUser = InitialChatModel(
                                            userUniqueId = contact.userUniqueId,
                                            userUniqueName = contact.userUniqueName
                                        )
                                        eventHandler(
                                            TcpScreenEvents.TcpChatItemClicked(
                                                selectedChattingUser
                                            )
                                        )
                                    },
                                    lastMessage = uiState.lastChattingUserMessage
                                )
                                if (index < data.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 66.dp, end = 12.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(0.4F)
                                    )
                                }
                            }
                        }
                    )
                }
                FabButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        // Offsets the FAB to compensate for CoordinatorLayout collapsing behaviour
                        .offset(y = ((-12).dp), x = ((-12).dp)),
                    isVisible = isVisible.value,
                    onClick = {}
                )
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
                contactsList = Resource.Success(
                    listOf(
                        ChattingUserEntity(
                            userUniqueId = "123",
                            userUniqueName = "Ahmed"
                        ),
                        ChattingUserEntity(
                            userUniqueId = "123",
                            userUniqueName = "Ahmed"
                        ),
                        ChattingUserEntity(
                            userUniqueId = "123",
                            userUniqueName = "Ahmed"
                        ),
                        ChattingUserEntity(
                            userUniqueId = "123",
                            userUniqueName = "Ahmed"
                        ),
                        ChattingUserEntity(
                            userUniqueId = "123",
                            userUniqueName = "Ahmed"
                        ),
                        ChattingUserEntity(
                            userUniqueId = "123",
                            userUniqueName = "Ahmed"
                        ),
                        ChattingUserEntity(
                            userUniqueId = "123",
                            userUniqueName = "Ahmed"
                        ),
                        ChattingUserEntity(
                            userUniqueId = "123",
                            userUniqueName = "Ahmed"
                        ),
                        ChattingUserEntity(
                            userUniqueId = "123",
                            userUniqueName = "Ahmed"
                        ),
                        ChattingUserEntity(
                            userUniqueId = "123",
                            userUniqueName = "Ahmed"
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
                contactsList = Resource.Loading()
            )
        )
    }
}
