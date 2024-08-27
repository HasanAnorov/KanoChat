package com.ierusalem.androchat.features_local.tcp.domain.state

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
data class ContactItem(
    val contactName:String,
    val phoneNumber:String,
    val isSelected: Boolean,
    val id: String = UUID.randomUUID().toString(),
)

@Immutable
data class ContactMessageItem(
    val contactName: String,
    val contactNumber: String
)