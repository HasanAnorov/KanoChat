package com.ierusalem.androchat.features_tcp.tcp.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.ierusalem.androchat.features_tcp.tcp.presentation.utils.TcpScreenEvents
import com.ierusalem.androchat.features_tcp.tcp.presentation.utils.TcpView
import com.ierusalem.androchat.features_tcp.tcp.domain.state.TcpScreenUiState
import com.ierusalem.androchat.features_tcp.tcp_chat.presentation.LocalConversationContent
import com.ierusalem.androchat.features_tcp.tcp_connections.ConnectionsContent
import com.ierusalem.androchat.features_tcp.tcp_instructions.InstructionsContent
import com.ierusalem.androchat.features_tcp.tcp_networking.NetworkingContent

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

            TcpView.INSTRUCTIONS -> {
                InstructionsContent(
                    modifier = Modifier.fillMaxSize(),
                )
            }

            TcpView.CHAT_ROOM -> {
                LocalConversationContent(
                    modifier = Modifier.fillMaxSize(),
                    uiState = state,
                    eventHandler = eventHandler
                )
            }
        }
    }
}