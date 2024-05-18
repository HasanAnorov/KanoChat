package com.ierusalem.androchat.features.home.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.features.home.domain.HomeScreenState
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
    eventHandler: (HomeScreenClickIntents) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = state.selectedTabIndex,
        pageCount = { state.tabItems.size }
    )
//    val isUserScrollEnabled = rememberSaveable { mutableStateOf(true) }
//    val nestedScrollConnection = remember {
//        object : NestedScrollConnection {
//
//            override fun onPreScroll(
//                available: Offset,
//                source: NestedScrollSource
//            ): Offset {
//                if (available.x <-1) {
//                    isUserScrollEnabled.value = true
//                }
//                if (available.x > 1) {
//                    isUserScrollEnabled.value = false
//                }
//                Log.d("ahi3646", "onPreScroll:  ${available.y}  ${available.x}")
//                return Offset.Zero
//            }
//        }
//    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            eventHandler(HomeScreenClickIntents.TabItemClicked(page))
        }
    }

    Scaffold(
        topBar = {
            Column {
                AndroChatAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = state.connectivityStatus.asString(),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 16.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { eventHandler(HomeScreenClickIntents.OnSearchClick) }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(onClick = { eventHandler(HomeScreenClickIntents.OnTcpClick) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.local),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    onNavIconPressed = {
                        eventHandler(HomeScreenClickIntents.NavIconClicked)
                    }
                )
            }
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
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(
                                        text = currentTab.asString(),
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
//                        .nestedScroll(nestedScrollConnection)
                        .fillMaxWidth()
                        .weight(1f),
                    //userScrollEnabled = isUserScrollEnabled.value,
                    state = pagerState,
                ) { pageCount ->
                    when (pageCount) {
                        0 -> AllChatsScreen(state = HomePreviewData.contactsSuccess)
                        1 -> ContactsScreen(
                            state = state.contacts,
                            intentReducer = eventHandler
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
            eventHandler = {},
        )
    }
}

@Preview
@Composable
fun HomeScreenPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        HomeScreen(
            state = HomeScreenState(),
            eventHandler = {},
        )
    }
}