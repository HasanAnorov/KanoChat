package com.ierusalem.androchat.features_local.tcp.presentation

import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.utils.UiText

enum class TcpView(val displayName: UiText, val testIdentifier: String) {
    CHATS(
        displayName = UiText.StringResource(R.string.chats),
        testIdentifier = MainTabIdentifiers.CHATS.name
    ),
    NETWORKING(
        displayName = UiText.StringResource(R.string.networking),
        testIdentifier = MainTabIdentifiers.NETWORKING.name),
    CONNECTIONS(
        displayName = UiText.StringResource(R.string.connections),
        testIdentifier = MainTabIdentifiers.CONNECTIONS.name
    ),
}

enum class MainTabIdentifiers{
    CHATS,
    NETWORKING,
    CONNECTIONS
}