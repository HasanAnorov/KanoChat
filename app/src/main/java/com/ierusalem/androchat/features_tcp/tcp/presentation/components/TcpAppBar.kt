package com.ierusalem.androchat.features_tcp.tcp.presentation.components

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.features_tcp.tcp.TcpView
import com.ierusalem.androchat.ui.components.AndroChatAppBar
import com.ierusalem.androchat.ui.theme.AndroChatTheme
import com.ierusalem.androchat.utils.UiText

@Composable
@CheckResult
fun rememberAllTabs(): SnapshotStateList<TcpView> {
    return remember { TcpView.entries.toMutableStateList() }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TcpAppBar(
    modifier: Modifier = Modifier,
    appName: UiText,
    onNavIconClick: () -> Unit,
    onSettingsIconClick: () -> Unit,
    pagerState: PagerState,
    allTabs: SnapshotStateList<TcpView>,
    onTabChanged: (TcpView) -> Unit
) {
    Column {
        AndroChatAppBar(
            modifier = modifier,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = appName.asString(),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp
                    )
                }
            },
            navIcon = Icons.AutoMirrored.Filled.ArrowBack,
            actions = {
                IconButton(onClick = onSettingsIconClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            onNavIconPressed = onNavIconClick
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
                    TcpTab(
                        isSelected = isSelected,
                        onSelected = { onTabChanged(tab) },
                        tab = tab
                    )
                }
            }
        )
    }
}

@Composable
fun TcpTab(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onSelected: () -> Unit,
    tab: TcpView
) {
    Tab(
        modifier = modifier,
        selected = isSelected,
        selectedContentColor = MaterialTheme.colorScheme.onBackground,
        unselectedContentColor = MaterialTheme.colorScheme.onBackground.copy(
            0.5F
        ),
        onClick = onSelected,
        text = {
            Text(
                text = tab.displayName.asString(),
                fontSize = 16.sp,
                style = MaterialTheme.typography.titleSmall
            )
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun PreviewAppBar() {
    val allTabs = rememberAllTabs()
    AndroChatTheme {
        TcpAppBar(
            appName = UiText.StringResource(R.string.local_connection),
            onNavIconClick = {},
            onSettingsIconClick = {},
            pagerState = rememberPagerState(
                initialPage = 0,
                initialPageOffsetFraction = 0F,
                pageCount = { allTabs.size },
            ),
            allTabs = rememberAllTabs(),
            onTabChanged = {}
        )
    }
}