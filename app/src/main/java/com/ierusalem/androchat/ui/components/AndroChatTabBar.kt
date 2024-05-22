package com.ierusalem.androchat.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.ierusalem.androchat.utils.UiText

@Composable
fun AndroChatTab(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onSelected: () -> Unit,
    tab: UiText
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
                text = tab.asString(),
                fontSize = 16.sp,
                style = MaterialTheme.typography.titleSmall
            )
        },
    )
}