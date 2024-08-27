package com.ierusalem.androchat.features_remote.home.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.features_remote.home.domain.HomeScreenState
import com.ierusalem.androchat.features_remote.home.presentation.components.HomeAppBar
import com.ierusalem.androchat.features_remote.home.presentation.components.rememberHomeAllTabs


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeScreenState,
    eventHandler: (HomeScreenClickIntents) -> Unit,
    allTabs: SnapshotStateList<HomeView>,
    pagerState: PagerState,
    onTabChanged: (HomeView) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { pv ->
        Column {
            HomeAppBar(
                title = state.connectivityStatus,
                allTabs = allTabs,
                pagerState = pagerState,
                onNavIconPressed = { eventHandler(HomeScreenClickIntents.NavIconClicked) },
                onTabChanged = { onTabChanged(it) },
                onTcpClick = { eventHandler(HomeScreenClickIntents.OnTcpClick) },
                onSearchClick = { eventHandler(HomeScreenClickIntents.OnSearchClick) }
            )
            HomeContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F)
                    .padding(bottom = pv.calculateBottomPadding())
                    // So this basically doesn't do anything since we handle the padding ourselves
                    // BUT, we don't just want to consume it because we DO actually care when using
                    // Modifier.navigationBarsPadding()
                    .heightIn(min = pv.calculateBottomPadding()),
                state = state,
                pagerState = pagerState,
                allTabs = allTabs,
                eventHandler = eventHandler
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun HomeScreenPreviewLight() {
    AndroChatTheme {
        val allTabs = rememberHomeAllTabs()
        HomeScreen(
            state = HomeScreenState(),
            eventHandler = {},
            allTabs = allTabs,
            pagerState = rememberPagerState(
                initialPage = 0,
                initialPageOffsetFraction = 0F,
                pageCount = { allTabs.size },
            ),
            onTabChanged = {},
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun HomeScreenPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        val allTabs = rememberHomeAllTabs()
        HomeScreen(
            state = HomeScreenState(),
            eventHandler = {},
            allTabs = allTabs,
            pagerState = rememberPagerState(
                initialPage = 0,
                initialPageOffsetFraction = 0F,
                pageCount = { allTabs.size },
            ),
            onTabChanged = {},
        )
    }
}