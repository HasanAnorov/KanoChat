package com.ierusalem.androchat.features_tcp.tcp.tcp.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.ierusalem.androchat.features_tcp.tcp.info.presentation.InfoScreen
import com.ierusalem.androchat.features_tcp.tcp.tcp.TcpView
import com.ierusalem.androchat.features_tcp.tcp.tcp.domain.TcpScreenUiState
import com.ierusalem.androchat.ui.tcp.ServerViewState
import com.ierusalem.androchat.utils.UiText

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TcpContent(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    allTabs: SnapshotStateList<TcpView>,
    appName: String,
    state: TcpScreenUiState,
    serverViewState: ServerViewState,
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
            TcpView.STATUS -> {

            }

            TcpView.CONNECTIONS -> {

            }

            TcpView.INFO -> {
                InfoScreen(
                    appName = appName,
                    state = state ,
                    serverViewState = ,
                    onTogglePasswordVisibility = { /*TODO*/ },
                    onShowQRCode = {}
                )
            }
        }
    }
}