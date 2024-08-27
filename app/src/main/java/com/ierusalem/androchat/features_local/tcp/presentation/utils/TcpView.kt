package com.ierusalem.androchat.features_local.tcp.presentation.utils

import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.utils.UiText

enum class TcpView(val displayName: UiText) {
    CONTACTS(UiText.StringResource(R.string.contacts)),
    NETWORKING(UiText.StringResource(R.string.networking)),
    CONNECTIONS(UiText.StringResource(R.string.connections)),
}