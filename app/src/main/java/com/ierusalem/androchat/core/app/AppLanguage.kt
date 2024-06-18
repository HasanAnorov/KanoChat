package com.ierusalem.androchat.core.app

import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.utils.UiText

enum class AppLanguage(val languageRes: UiText, var isSelected: Boolean) {
    English(languageRes = UiText.StringResource(R.string.english),  isSelected = false),
    Russian(languageRes = UiText.StringResource(R.string.russian), isSelected =  true),
}
