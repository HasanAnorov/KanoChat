package com.ierusalem.androchat.features.home.presentation.contacts

import androidx.compose.runtime.Immutable

sealed interface ContactsScreen {

    data object Loading : ContactsScreen

    data class Success(val content: List<ContactsScreenData>) : ContactsScreen

    data class Error(val message: String) : ContactsScreen

}

@Immutable
data class ContactsScreenData(
    val contactName: String,
    val lastMessage: String,
    val messageTime: String,
    val unreadMessageCount: Int,
)