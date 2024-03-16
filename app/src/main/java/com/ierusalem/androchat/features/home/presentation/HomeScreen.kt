package com.ierusalem.androchat.features.home.presentation

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import com.ierusalem.androchat.ui.components.AndroChatAppBar
import com.ierusalem.androchat.ui.theme.AndroChatTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    state: HomeScreenState,
    modifier: Modifier = Modifier,
    intentReducer: (HomeScreenClickIntents) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val pagerState = rememberPagerState {
        state.tabItems.size
    }
    var selectedTabIndex by remember{
        mutableIntStateOf(0)
    }
//    LaunchedEffect(selectedTabIndex) {
//        pagerState.animateScrollToPage(selectedTabIndex)
//    }
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            selectedTabIndex = pagerState.currentPage
        }
    }

    Scaffold(
        topBar = {
            AndroChatAppBar(
                title = { },
                scrollBehavior = scrollBehavior,
                onNavIconPressed = {
                    intentReducer(HomeScreenClickIntents.NavIconClicked)
                }
            )
        },
        // Exclude ime and navigation bar padding so this can be added by the UserInput composable
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            content = {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    state.tabItems.forEachIndexed { index, currentTab ->
                        Log.d("ahi3646", "HomeScreen: $index $selectedTabIndex ")
                        Tab(
                            selected = selectedTabIndex == index,
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.outline,
                            onClick = {
                                selectedTabIndex = index
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(text = currentTab) },

                            )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = state.tabItems[it])
                    }
                }
            }
        )
//        Column(
//            modifier = Modifier
//                .padding(paddingValues)
//                .fillMaxSize(),
//        ) {
//            TabRow(
//                selectedTabIndex = state.selectedTabIndex,
//                divider = {},
//                tabs = {
//                    state.tabItems.forEachIndexed { index, tabItem ->
//                        Tab(
//                            selected = index == state.selectedTabIndex,
//                            onClick = {
//                                scope.launch {
//                                    pagerState.animateScrollToPage(index)
//                                }
////                                intentReducer(HomeScreenClickIntents.TabItemClicked(index))
//                            },
//                            content = {
//                                Text(
//                                    modifier = Modifier.padding(bottom = 12.dp, top = 8.dp),
//                                    text = tabItem,
//                                    style = MaterialTheme.typography.titleMedium
//                                )
//                            }
//                        )
//                    }
//                }
//            )
//            HorizontalPager(
//                state = pagerState,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1F),
//            ) { index ->
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center,
//                    content = {
//                        Text(text = state.tabItems[index])
//                    }
//                )
//            }
//        }
    }
}

@Preview
@Composable
fun HomeScreenPreviewLight() {
    AndroChatTheme {
        HomeScreen(
            state = HomeScreenState(),
            modifier = Modifier,
            intentReducer = {}
        )
    }
}

@Preview
@Composable
fun HomeScreenPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        HomeScreen(
            modifier = Modifier,
            state = HomeScreenState(),
            intentReducer = {}
        )
    }
}