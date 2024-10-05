package com.ierusalem.androchat.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ierusalem.androchat.core.ui.animations.AnimatedShimmer
import com.ierusalem.androchat.core.ui.animations.ChatShimmerItem
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme

@Composable
fun LoadingScreen(modifier: Modifier = Modifier, isForChat: Boolean = false) {
    Column(
        modifier = modifier
    ) {
        repeat(17) {
            if (isForChat) {
                ChatShimmerItem()
            } else {
                AnimatedShimmer()
            }
        }
    }
}

@Preview
@Composable
fun LoadingScreenPreview() {
    AndroChatTheme {
        LoadingScreen()
    }
}

@Preview
@Composable
fun LoadingScreenPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        LoadingScreen()
    }
}