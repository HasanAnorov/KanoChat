package com.ierusalem.androchat.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.ui.theme.AndroChatTheme

/**
 * AndroChatAppBar
 *
 * @author A.H.I "andro" on 07/03/2024
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroChatAppBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavIconPressed: () -> Unit = { },
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        actions = actions,
        title = title,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            AndroChatIcon(
                contentDescription = stringResource(id = R.string.navigation_drawer_open),
                modifier = Modifier
                    .size(64.dp)
                    .clickable(onClick = onNavIconPressed)
                    .padding(16.dp)
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AndroChatAppBarPreview() {
    AndroChatTheme {
        AndroChatAppBar(title = { Text("Preview!") })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AndroChatAppBarPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        AndroChatAppBar(title = { Text("Preview!") })
    }
}
