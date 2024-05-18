package com.ierusalem.androchat.features.home.presentation

import com.ierusalem.androchat.R
import com.ierusalem.androchat.utils.UiText

enum class MainView(val displayName: UiText) {
    ALL(UiText.StringResource(R.string.all)),
    CONTACTS(UiText.StringResource(R.string.contacts)),
    GROUPS(UiText.StringResource(R.string.groups)),
}
