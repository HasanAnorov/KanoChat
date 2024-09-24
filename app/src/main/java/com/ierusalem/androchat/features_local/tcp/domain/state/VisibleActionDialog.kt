package com.ierusalem.androchat.features_local.tcp.domain.state

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.ui.graphics.vector.ImageVector
import com.ierusalem.androchat.R

sealed interface VisibleActionDialogs {

    val dialogTitle: Int
    val dialogMessage: Int
    val icon: ImageVector
    val positiveButtonText: Int
    val negativeButtonText: Int
    val onPositiveButtonClick: () -> Unit
    val onNegativeButtonClick: () -> Unit

    data class WifiEnableRequest(
        override val dialogTitle: Int = R.string.wifi_not_enabled,
        override val dialogMessage: Int = R.string.wifi_not_enabled_message,
        override val icon: ImageVector = Icons.Default.WifiOff,
        override val positiveButtonText: Int = R.string.enable,
        override val negativeButtonText: Int = R.string.dismiss,
        override val onPositiveButtonClick: () -> Unit = {},
        override val onNegativeButtonClick: () -> Unit = {}
    ) : VisibleActionDialogs

    data class LogoutRequest(
        override val dialogTitle: Int = R.string.are_you_sure,
        override val dialogMessage: Int = R.string.logout_message,
        override val icon: ImageVector = Icons.Default.ErrorOutline,
        override val positiveButtonText: Int = R.string.logout,
        override val negativeButtonText: Int = R.string.cancel,
        override val onPositiveButtonClick: () -> Unit = {},
        override val onNegativeButtonClick: () -> Unit = {}
    ) : VisibleActionDialogs
}