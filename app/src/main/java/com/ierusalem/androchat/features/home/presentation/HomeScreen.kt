package com.ierusalem.androchat.features.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ierusalem.androchat.ui.theme.AndroChatTheme

@Composable
fun HomeScreen(){

}

@Preview
@Composable
fun HomeScreenPreviewLight(){
    AndroChatTheme {
        HomeScreen()
    }
}

@Preview
@Composable
fun HomeScreenPreviewDark(){
    AndroChatTheme(isDarkTheme = true) {
        HomeScreen()
    }
}