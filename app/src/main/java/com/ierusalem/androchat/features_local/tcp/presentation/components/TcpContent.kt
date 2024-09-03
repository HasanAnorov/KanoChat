package com.ierusalem.androchat.features_local.tcp.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.ierusalem.androchat.features_local.tcp.presentation.TcpScreenEvents
import com.ierusalem.androchat.features_local.tcp.presentation.TcpView
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenUiState
import com.ierusalem.androchat.features_local.tcp.presentation.tcp_connection.ConnectionsContent
import com.ierusalem.androchat.features_local.tcp.presentation.tcp_chats.ChatsScreen
import com.ierusalem.androchat.features_local.tcp.presentation.tcp_networking.NetworkingContent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TcpContent(
    modifier: Modifier = Modifier,
    state: TcpScreenUiState,
    allTabs: SnapshotStateList<TcpView>,
    pagerState: PagerState,
    eventHandler: (TcpScreenEvents) -> Unit
) {
    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        userScrollEnabled = !state.isRecording
    ) { page ->
        val screen = remember(
            allTabs,
            page,
        ) {
            allTabs[page]
        }
        when (screen) {

            TcpView.CHATS -> {
                ChatsScreen(
                    modifier = Modifier.fillMaxSize(),
                    eventHandler = eventHandler,
                    uiState = state,
                )
            }

            TcpView.NETWORKING -> {
                NetworkingContent(
                    modifier = Modifier.fillMaxSize(),
                    eventHandler = eventHandler,
                    state = state
                )
            }

            TcpView.CONNECTIONS -> {
                ConnectionsContent(
                    modifier = Modifier.fillMaxSize(),
                    eventHandler = eventHandler,
                    state = state
                )
            }

        }
    }
}