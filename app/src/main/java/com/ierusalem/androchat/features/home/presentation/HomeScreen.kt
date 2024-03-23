package com.ierusalem.androchat.features.home.presentation

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.ierusalem.androchat.features.home.presentation.all.AllChatsScreen
import com.ierusalem.androchat.features.home.presentation.contacts.ContactsScreen
import com.ierusalem.androchat.features.home.presentation.contacts.ErrorType
import com.ierusalem.androchat.features.home.presentation.group.GroupScreen
import com.ierusalem.androchat.ui.components.AndroChatAppBar
import com.ierusalem.androchat.ui.theme.AndroChatTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    state: HomeScreenState,
    intentReducer: (HomeScreenClickIntents) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = state.selectedTabIndex,
        pageCount = { state.tabItems.size }
    )
    val isUserScrollEnabled = rememberSaveable { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {

            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.x <-1) {
                    isUserScrollEnabled.value = true
                }
                if (available.x > 1) {
                    isUserScrollEnabled.value = false
                }
                Log.d("ahi3646", "onPreScroll:  ${available.y}  ${available.x}")
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            intentReducer(HomeScreenClickIntents.TabItemClicked(page))
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
        }
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
                    divider = {},
                    tabs = {
                        state.tabItems.forEachIndexed { index, currentTab ->
                            Tab(
                                selected = state.selectedTabIndex == index,
                                selectedContentColor = MaterialTheme.colorScheme.onBackground,
                                unselectedContentColor = MaterialTheme.colorScheme.onBackground.copy(
                                    0.5F
                                ),
                                onClick = {
                                    scope.launch {
                                        //intentReducer(HomeScreenClickIntents.TabItemClicked(index))
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
                    modifier = Modifier
                        .nestedScroll(nestedScrollConnection)
                        .fillMaxWidth()
                        .weight(1f),
                    //userScrollEnabled = isUserScrollEnabled.value,
                    state = pagerState,
                ) { pageCount ->
                    when (pageCount) {
                        0 -> AllChatsScreen(state = ContactsScreen.Loading)

                        1 -> ContactsScreen(
                            state = state.contacts,
                            intentReducer = intentReducer
                        )

                        2 -> GroupScreen(state = ContactsScreen.Error(ErrorType.NetworkError))
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
            intentReducer = {},
        )
    }
}

@Preview
@Composable
fun HomeScreenPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        HomeScreen(
            state = HomeScreenState(),
            intentReducer = {},
        )
    }
}