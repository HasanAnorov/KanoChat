package com.ierusalem.androchat.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme

@Composable
fun EmptyScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Text(
            modifier = Modifier,
            style = MaterialTheme.typography.bodyLarge,
            text = stringResource(R.string.no_data_found))
    }
}

@Preview
@Composable
private fun PreviewEmptyScreen() {
    AndroChatTheme {
        Surface {
            EmptyScreen()
        }
    }
}

@Preview
@Composable
private fun PreviewDarkEmptyScreen() {
    AndroChatTheme(isDarkTheme = true) {
        Surface {
            EmptyScreen()
        }
    }
}