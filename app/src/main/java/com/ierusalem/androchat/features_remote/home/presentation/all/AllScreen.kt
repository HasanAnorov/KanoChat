package com.ierusalem.androchat.features_remote.home.presentation.all

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.core.ui.components.ErrorScreen
import com.ierusalem.androchat.core.ui.components.LoadingScreen
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.features_remote.home.presentation.contacts.ContactItem
import com.ierusalem.androchat.features_remote.home.presentation.contacts.ContactsScreen
import com.ierusalem.androchat.features_remote.home.presentation.contacts.ErrorType

@Composable
fun AllChatsScreen(
    modifier: Modifier = Modifier,
    state: ContactsScreen
){
    when (state) {
        ContactsScreen.Loading -> LoadingScreen(modifier)

        is ContactsScreen.Success -> {
            val data = state.content
            Box(modifier = modifier) {
                if (data.isEmpty()) {
                    Log.d("ahi3646", "ContactsScreen: Data is empty ")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        content = {
                            itemsIndexed(data) { index, contact ->
                                ContactItem(
                                    contact = contact,
                                    modifier = Modifier,
                                    onClick = {}
                                )
                                if (index < data.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 66.dp, end = 12.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(0.4F)
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }

        is ContactsScreen.Error -> ErrorScreen(state.error, onRetryClick = {})
    }

}

@Preview
@Composable
fun AllChatsScreenPreview() {
    AndroChatTheme {
        AllChatsScreen(
            modifier = Modifier,
            state = ContactsScreen.Error(ErrorType.NetworkError)
        )
    }
}

@Preview
@Composable
fun AllChatsScreenPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        AllChatsScreen(
            modifier = Modifier,
            state = ContactsScreen.Error(ErrorType.NetworkError)
        )
    }
}
