package com.ierusalem.androchat.core.data

import com.ierusalem.androchat.R
import com.ierusalem.androchat.features_remote.profile.ProfileScreenState

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
    userId = "Owner",
    photo = R.drawable.mclaren,
    name = "Andro Chat",
    status = "Online",
    displayName = "andro",
    position = "Cyber security learner\nLove coding and hacking",
    twitter = "twitter.com/andro",
    timeZone = "In your timezone",
    commonChannels = null
)
