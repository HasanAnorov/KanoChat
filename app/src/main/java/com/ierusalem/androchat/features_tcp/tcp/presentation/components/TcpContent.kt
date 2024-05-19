package com.ierusalem.androchat.features_tcp.tcp.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.ierusalem.androchat.features_tcp.tcp.TcpScreenEvents
import com.ierusalem.androchat.features_tcp.tcp.TcpView
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpScreenUiState
import com.ierusalem.androchat.features_tcp.tcp_client.ClientContent
import com.ierusalem.androchat.features_tcp.tcp_server.HotSpotContent

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
    ) { page ->
        val screen =
            remember(
                allTabs,
                page,
            ) {
                allTabs[page]
            }
        when (screen) {
            TcpView.HOTSPOT -> {
                HotSpotContent(
                    modifier = Modifier.fillMaxSize(),
                    eventHandler = eventHandler,
                    state = state
                )
            }

            TcpView.CONNECTIONS -> {
                ClientContent(
                    modifier = Modifier.fillMaxSize(),
                    eventHandler = eventHandler,
                    state = state
                )
            }
        }
    }
}