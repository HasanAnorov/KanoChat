package com.ierusalem.androchat.features_local.tcp.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.UiText
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenUiState
import com.ierusalem.androchat.features_local.tcp.presentation.components.NetworkErrorDialog
import com.ierusalem.androchat.features_local.tcp.presentation.components.TcpAppBar
import com.ierusalem.androchat.features_local.tcp.presentation.components.TcpContent
import com.ierusalem.androchat.features_local.tcp.presentation.components.rememberTcpAllTabs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TcpScreen(
    modifier: Modifier = Modifier,
    eventHandler: (TcpScreenEvents) -> Unit,
    allTabs: SnapshotStateList<TcpView>,
    pagerState: PagerState,
    uiState: TcpScreenUiState,
    onTabChanged: (TcpView) -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) { pv ->
        if (uiState.hasTcpDialogErrorOccurred != null) {
            NetworkErrorDialog(
                onDismissRequest = { eventHandler(TcpScreenEvents.OnDialogErrorOccurred(null)) },
                onConfirmation = { eventHandler(TcpScreenEvents.OnDialogErrorOccurred(null)) },
                dialogTitle = uiState.hasTcpDialogErrorOccurred.title,
                dialogText = uiState.hasTcpDialogErrorOccurred.message,
                icon = painterResource(id = uiState.hasTcpDialogErrorOccurred.icon)
            )
        }

        Column {
            TcpAppBar(
                title = UiText.StringResource(R.string.local_connection),
                onNavIconClick = { eventHandler(TcpScreenEvents.OnNavIconClick) },
                onSettingsIconClick = { eventHandler(TcpScreenEvents.OnSettingIconClick) },
                allTabs = allTabs,
                pagerState = pagerState,
                onTabChanged = { onTabChanged(it) }
            )
            TcpContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F)
                    // So this basically doesn't do anything since we handle the padding ourselves
                    // BUT, we don't just want to consume it because we DO actually care when using
                    // Modifier.navigationBarsPadding()
                    .heightIn(min = pv.calculateBottomPadding()),
                allTabs = allTabs,
                pagerState = pagerState,
                eventHandler = eventHandler,
                state = uiState,
                onCreateNetworkClick = { onTabChanged(TcpView.NETWORKING) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun TcpScreenPreview() {
    AndroChatTheme {
        val allTabs = rememberTcpAllTabs()
        TcpScreen(
            eventHandler = {},
            allTabs = allTabs,
            pagerState = rememberPagerState(
                initialPage = 0,
                initialPageOffsetFraction = 0F,
                pageCount = { allTabs.size },
            ),
            onTabChanged = {},
            uiState = TcpScreenUiState()
        )
    }
}