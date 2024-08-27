package com.ierusalem.androchat.features_remote.home.presentation.components

import androidx.annotation.CheckResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.components.AndroChatAppBar
import com.ierusalem.androchat.core.ui.components.AndroChatTab
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.UiText
import com.ierusalem.androchat.features_remote.home.presentation.HomeView

@Composable
@CheckResult
fun rememberHomeAllTabs(): SnapshotStateList<HomeView> {
    return remember { HomeView.entries.toMutableStateList() }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeAppBar(
    modifier: Modifier = Modifier,
    title: UiText,
    pagerState: PagerState,
    onTabChanged: (HomeView) -> Unit = {},
    allTabs: SnapshotStateList<HomeView>,
    onNavIconPressed: () -> Unit,
    onSearchClick: () -> Unit,
    onTcpClick: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        AndroChatAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = title.asString(),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp
                    )
                }
            },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = onTcpClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.local),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            onNavIconPressed = onNavIconPressed
        )

        val currentPage = pagerState.currentPage
        TabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = currentPage,
            indicator = { tabPositions ->
                if (currentPage < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[currentPage]),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            contentColor = MaterialTheme.colorScheme.onBackground,
            divider = {},
            tabs = {
                for (index in allTabs.indices) {
                    val tab = allTabs[index]
                    val isSelected =
                        remember(
                            index,
                            currentPage,
                        ) {
                            index == currentPage
                        }
                    AndroChatTab(
                        isSelected = isSelected,
                        onSelected = { onTabChanged(tab) },
                        tab = tab.displayName
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun HomeAppBarPreview() {
    AndroChatTheme {
        val allTabs = rememberHomeAllTabs()
        HomeAppBar(
            title = UiText.StringResource(R.string.app_name),
            pagerState = rememberPagerState(
                initialPage = 0,
                initialPageOffsetFraction = 0F,
                pageCount = { allTabs.size },
            ),
            allTabs = rememberHomeAllTabs(),
            onNavIconPressed = {},
            onSearchClick = {},
            onTcpClick = {}
        )
    }
}