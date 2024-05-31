package com.ierusalem.androchat.features_tcp.tcp

import com.ierusalem.androchat.R
import com.ierusalem.androchat.utils.UiText

enum class TcpView(val displayName: UiText) {
    GROUP_CONNECTION(UiText.StringResource(R.string.group_connection)),
    P2P_CONNECTION(UiText.StringResource(R.string.nearby_connection)),
    CONNECTIONS(UiText.StringResource(R.string.connections)),
    CHAT_ROOM(UiText.StringResource(R.string.chat_room)),
    INSTRUCTIONS(UiText.StringResource(R.string.instructions))
}