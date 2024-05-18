package com.ierusalem.androchat.features_tcp.tcp.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ierusalem.androchat.features_tcp.tcp.TcpView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TcpContent(
    modifier: Modifier = Modifier,
    allTabs: SnapshotStateList<TcpView>,
    pagerState: PagerState,
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Hotspot")
                }
            }

            TcpView.CONNECTIONS -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Connections")
                }
            }
        }
    }
}