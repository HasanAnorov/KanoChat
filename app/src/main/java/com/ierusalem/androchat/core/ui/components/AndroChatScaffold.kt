package com.ierusalem.androchat.core.ui.components

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue.Closed
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.features.home.presentation.HomeScreenClickIntents

/**
 * AndroChatDrawer
 *
 * @author A.H.I "andro" on 07/03/2024
 */

@Composable
fun AndroChatDrawer(
    drawerState: DrawerState = rememberDrawerState(initialValue = Closed),
    onDrawerItemClick: (HomeScreenClickIntents) -> Unit,
    content: @Composable () -> Unit
) {
    AndroChatTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    AndroChatDrawerContent(
                        onDrawerItemClick = onDrawerItemClick
                    )
                }
            },
            content = content
        )
    }
}
