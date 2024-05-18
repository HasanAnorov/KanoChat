package com.ierusalem.androchat.features_tcp.tcp

import com.ierusalem.androchat.R
import com.ierusalem.androchat.utils.UiText

enum class TcpView(val displayName: UiText) {
    HOTSPOT(UiText.StringResource(R.string.hostspot)),
    CONNECTIONS(UiText.StringResource(R.string.connections))
}