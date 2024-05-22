package com.ierusalem.androchat.features.home.presentation

import com.ierusalem.androchat.R
import com.ierusalem.androchat.utils.UiText

enum class HomeView(val displayName: UiText) {
    All(UiText.StringResource(R.string.all)),
    Contacts(UiText.StringResource(R.string.contacts)),
    Groups(UiText.StringResource(R.string.groups))
}