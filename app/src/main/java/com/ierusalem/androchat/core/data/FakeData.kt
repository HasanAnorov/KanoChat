/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ierusalem.androchat.core.data

import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.data.EMOJIS.EMOJI_FLAMINGO
import com.ierusalem.androchat.core.data.EMOJIS.EMOJI_POINTS
import com.ierusalem.androchat.features.conversation.presentation.components.ConversationUiState
import com.ierusalem.androchat.features.conversation.presentation.components.Message
import com.ierusalem.androchat.features.profile.ProfileScreenState

private val initialMessages = listOf(
    Message(
        "you",
        "Check it out!",
        "8:07 PM"
    ),
    Message(
        "you",
        "Thank you!$EMOJI_POINTS",
        "8:06 PM",
    ),
    Message(
        "you",
        "Rate my pc setup, I can send even images $EMOJI_FLAMINGO",
        "8:06 PM",
        R.drawable.setup
    ),
    Message(
        "Imaginary Person",
        "You can use all the same stuff",
        "8:05 PM"
    ),
    Message(
        "Imaginary Person",
        "@andro Like telegram `You can tag a person with his display name` " +
                "use @ before `his display name`'",
        "8:05 PM"
    ),
    Message(
        "Imaginary Person",
        "“Bot tarmog‘i” – bu dasturiy ta’minot bo‘lib, " +
                "bir nechta Internet tarmog‘iga ulangan qurilmalardan tashkil topadi. " +
                "“Bot-tarmog‘i” ichidagi zararli dastur jabrlanuvchining qurilmasiga yashirincha " +
                "o‘rnatiladi hamda tajovuzkorga zararlangan kompyuter resurslaridan foydalangan " +
                "holda muayyan harakatlarni amalga oshirishga imkon yaratib beradi." +
                " $EMOJI_POINTS https://csec.uz/uz/recomendations/antivirus-ilovalari/",
        "8:04 PM"
    ),
    Message(
        "you",
        "“Bot-tarmoq”larga qarshi kurashning dolzarbligi va ahamiyatini inobatga olgan holda," +
                " Windows va Android operatsion tizimlarida ishlovchi zararlangan (mobil telefonlar," +
                " planshetlar, kompyuterlar va boshqalar) qurilmalarni virusdan tozalash va " +
                "ehtimoliy zararlanishlarni oldini olishda antivirus dasturiy " +
                "ta’minotlaridan foydalanishingizni so‘raymiz.",
        "8:03 PM"
    )
)

val exampleUiState = ConversationUiState(
    initialMessages = initialMessages,
    channelName = "#cyber security learners",
    channelMembers = 42
)

/**
 * Example colleague profile
 */
val colleagueProfile = ProfileScreenState(
    userId = "12345",
    photo = R.drawable.be_doer,
    name = "Imaginary Person",
    status = "Away",
    displayName = "person",
    position = "Just a imaginary person",
    twitter = "twitter.com/imaginary_person",
    timeZone = "12:25 AM local time (Eastern Daylight Time)",
    commonChannels = "2"
)

/**
 * Example "you" profile.
 */
val meProfile = ProfileScreenState(
    userId = "you",
    photo = R.drawable.mclaren,
    name = "Andro Chat",
    status = "Online",
    displayName = "andro",
    position = "Cyber security learner\nLove coding and hacking",
    twitter = "twitter.com/andro",
    timeZone = "In your timezone",
    commonChannels = null
)

object EMOJIS {
    // EMOJI 15
    const val EMOJI_PINK_HEART = "\uD83E\uDE77"

    // EMOJI 14 🫠
    const val EMOJI_MELTING = "\uD83E\uDEE0"

    // ANDROID 13.1 😶‍🌫️
    const val EMOJI_CLOUDS = "\uD83D\uDE36\u200D\uD83C\uDF2B️"

    // ANDROID 12.0 🦩
    const val EMOJI_FLAMINGO = "\uD83E\uDDA9"

    // ANDROID 12.0  👉
    const val EMOJI_POINTS = " \uD83D\uDC49"
}
