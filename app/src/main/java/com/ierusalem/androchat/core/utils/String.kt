package com.ierusalem.androchat.core.utils

import com.google.gson.Gson
import com.ierusalem.androchat.features.auth.register.domain.model.Message

//TODO user this later
fun String.toMessage(gson: Gson): Message.TextMessage {
    return gson.fromJson(this, Message.TextMessage::class.java)
}