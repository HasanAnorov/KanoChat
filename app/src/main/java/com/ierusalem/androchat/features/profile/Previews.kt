package com.ierusalem.androchat.features.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ierusalem.androchat.data.colleagueProfile
import com.ierusalem.androchat.data.meProfile
import com.ierusalem.androchat.ui.theme.AndroChatTheme

@Preview(widthDp = 340, name = "340 width - Me")
@Composable
fun ProfilePreview340() {
    AndroChatTheme {
        ProfileScreen(meProfile)
    }
}

@Preview(widthDp = 480, name = "480 width - Me")
@Composable
fun ProfilePreview480Me() {
    AndroChatTheme {
        ProfileScreen(meProfile)
    }
}

@Preview(widthDp = 480, name = "480 width - Other")
@Composable
fun ProfilePreview480Other() {
    AndroChatTheme {
        ProfileScreen(colleagueProfile)
    }
}
@Preview(widthDp = 340, name = "340 width - Me - Dark")
@Composable
fun ProfilePreview340MeDark() {
    AndroChatTheme(isDarkTheme = true) {
        ProfileScreen(meProfile)
    }
}

@Preview(widthDp = 480, name = "480 width - Me - Dark")
@Composable
fun ProfilePreview480MeDark() {
    AndroChatTheme(isDarkTheme = true) {
        ProfileScreen(meProfile)
    }
}

@Preview(widthDp = 480, name = "480 width - Other - Dark")
@Composable
fun ProfilePreview480OtherDark() {
    AndroChatTheme(isDarkTheme = true) {
        ProfileScreen(colleagueProfile)
    }
}
