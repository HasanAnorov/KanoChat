package com.ierusalem.androchat.features_remote.home.presentation

import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.utils.UiText

enum class HomeView(val displayName: UiText) {
    All(UiText.StringResource(R.string.all)),
    Contacts(UiText.StringResource(R.string.contacts)),
    Groups(UiText.StringResource(R.string.groups))
}