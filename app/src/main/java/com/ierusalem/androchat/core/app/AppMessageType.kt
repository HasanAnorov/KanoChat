package com.ierusalem.androchat.core.app

/**
 * When reading data in bytes, we need to define our own protocol for communication
 * between server and client. The simplest protocol which we can define is
 * called TLV (Type Length Value). It means that every message written to
 * the socket is in the form of the Type Length Value.
 *
 * So we define every message sent as:
 *
 * A 1 byte character that represents the data type, like s for String
 * A 4 byte integer that indicates the length to the data
 * And then the actual data, whose length was just indicated
 * */

enum class AppMessageType(val identifier: Char) {
    INITIAL('i'),
    TEXT('t'),
    FILE('f'),
    CONTACT('c'),

    UNKNOWN('u');

    companion object {
        fun fromChar(char: Char): AppMessageType {
            return entries.find { it.identifier == char } ?: UNKNOWN
        }
    }
}