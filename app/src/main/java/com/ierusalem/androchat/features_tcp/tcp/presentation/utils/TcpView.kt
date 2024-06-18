package com.ierusalem.androchat.features_tcp.tcp.presentation.utils

import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.utils.UiText

enum class TcpView(val displayName: UiText) {
    NETWORKING(UiText.StringResource(R.string.networking)),
    CONNECTIONS(UiText.StringResource(R.string.connections)),
    CHAT_ROOM(UiText.StringResource(R.string.chat_room)),
    INSTRUCTIONS(UiText.StringResource(R.string.instructions))
}