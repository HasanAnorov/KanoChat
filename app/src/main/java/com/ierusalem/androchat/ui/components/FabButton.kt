package com.ierusalem.androchat.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ierusalem.androchat.ui.theme.AndroChatTheme

@Composable
fun FabButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isVisible: Boolean
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it * 2 }),
        exit = slideOutVertically(targetOffsetY = { it * 2 }),
    ) {
        FloatingActionButton(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = { onClick() },
            shape = FloatingActionButtonDefaults.largeShape
        ) {
            Icon(Icons.Filled.Edit, "Floating action button.")
        }
    }
}

@Preview
@Composable
fun FabButtonLightPreview() {
    AndroChatTheme {
        FabButton(
            isVisible = true,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun FabButtonDarkPreview() {
    AndroChatTheme {
        FabButton(
            isVisible = false,
            onClick = {}
        )
    }
}