package com.ierusalem.androchat.features_local.tcp.presentation

import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.utils.UiText

enum class TcpView(val displayName: UiText) {
    CHATS(UiText.StringResource(R.string.chats)),
    NETWORKING(UiText.StringResource(R.string.networking)),
    CONNECTIONS(UiText.StringResource(R.string.connections)),
}