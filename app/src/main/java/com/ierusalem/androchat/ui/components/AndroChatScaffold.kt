package com.ierusalem.androchat.ui.components

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue.Closed
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import com.ierusalem.androchat.ui.theme.AndroChatTheme

/**
 * AndroChatDrawer
 *
 * @author A.H.I "andro" on 07/03/2024
 */

@Composable
fun AndroChatDrawer(
    drawerState: DrawerState = rememberDrawerState(initialValue = Closed),
    onChatClicked: (String) -> Unit,
    content: @Composable () -> Unit
) {
    AndroChatTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    AndroChatDrawerContent(
                        onChatClicked = onChatClicked
                    )
                }
            },
            content = content
        )
    }
}
