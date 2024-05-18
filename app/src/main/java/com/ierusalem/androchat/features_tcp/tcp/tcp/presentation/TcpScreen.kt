package com.ierusalem.androchat.features_tcp.tcp.tcp.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.ierusalem.androchat.features_tcp.tcp.tcp.TcpView
import com.ierusalem.androchat.features_tcp.tcp.tcp.presentation.components.TcpTopBar
import com.ierusalem.androchat.utils.UiText

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TcpScreen(
    modifier: Modifier = Modifier,
    appName: UiText,
    pagerState: PagerState,
    allTabs: SnapshotStateList<TcpView>,

    onNavIconClick : () -> Unit,
    onSettingsClick : () -> Unit,
    onTabClick : (TcpView) -> Unit
    ) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) {
        Column {
            TcpTopBar(
                title = appName,
                pagerState = pagerState,
                allTabs = allTabs,
                onNavIconClick =  onNavIconClick ,
                onSettingsClick =  onSettingsClick ,
                onTabSelected = onTabClick
            )

        }
    }
}


