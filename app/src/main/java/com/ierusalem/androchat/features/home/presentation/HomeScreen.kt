package com.ierusalem.androchat.features.home.presentation

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
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.ierusalem.androchat.features.home.presentation.contacts.ContactsScreen
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
    val pagerState = rememberPagerState {
        state.tabItems.size
    }
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            intentReducer(HomeScreenClickIntents.TabItemClicked(pagerState.currentPage))
        }
    }

    Scaffold(
        topBar = {
            AndroChatAppBar(
                title = { },
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
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            content = {
                TabRow(
                    modifier = Modifier.fillMaxWidth(),
                    selectedTabIndex = state.selectedTabIndex,
                    indicator = { tabPositions ->
                        if (state.selectedTabIndex < tabPositions.size) {
                            SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTabIndex]),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    divider = {

                    },
                    tabs = {
                        state.tabItems.forEachIndexed { index, currentTab ->
                            Tab(
                                selected = state.selectedTabIndex == index,
                                selectedContentColor = MaterialTheme.colorScheme.onBackground,
                                unselectedContentColor = MaterialTheme.colorScheme.onBackground.copy(
                                    0.5F
                                ),
                                onClick = {
                                    intentReducer(HomeScreenClickIntents.TabItemClicked(index))
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(
                                        text = currentTab,
                                        fontSize = 16.sp,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                },
                            )
                        }
                    }
                )
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { pageCount ->
                    when (pageCount) {
                        0 -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                                content = {
                                    Text(text = "All")
                                }
                            )
                        }

                        1 -> ContactsScreen(state = state.contacts)

                        2 -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                                content = {
                                    Text(text = "Groups")
                                }
                            )
                        }
                    }
                }
            }
        )
    }
}

@Preview
@Composable
fun HomeScreenPreviewLight() {
    AndroChatTheme {
        HomeScreen(
            state = HomeScreenState(),
            modifier = Modifier,
            intentReducer = {},
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
            intentReducer = {},
        )
    }
}