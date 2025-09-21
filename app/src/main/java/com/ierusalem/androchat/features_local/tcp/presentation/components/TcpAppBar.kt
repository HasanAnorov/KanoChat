package com.ierusalem.androchat.features_local.tcp.presentation.components

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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.components.AdaptiveTcpTabRow
import com.ierusalem.androchat.core.ui.components.AndroChatAppBar
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.UiText
import com.ierusalem.androchat.features_local.tcp.presentation.TcpView

@Composable
@CheckResult
fun rememberTcpAllTabs(): SnapshotStateList<TcpView> {
    return remember { TcpView.entries.toMutableStateList() }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun TcpAppBar(
    modifier: Modifier = Modifier,
    title: UiText,
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
                        text = title.asString(),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp
                    )
                }
            },
            navIcon = null,
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

        AdaptiveTcpTabRow(
            allTabs = allTabs,
            currentPage = currentPage,
            onTabChanged = onTabChanged,
            minTabWidth = 120.dp
        )

    }
}


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun PreviewAppBar() {
    val allTabs = rememberTcpAllTabs()
    AndroChatTheme {
        TcpAppBar(
            title = UiText.StringResource(R.string.local_connection),
            onNavIconClick = {},
            onSettingsIconClick = {},
            pagerState = rememberPagerState(
                initialPage = 0,
                initialPageOffsetFraction = 0F,
                pageCount = { allTabs.size },
            ),
            allTabs = rememberTcpAllTabs(),
            onTabChanged = {}
        )
    }
}