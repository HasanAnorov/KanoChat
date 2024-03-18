package com.ierusalem.androchat.features.home.presentation.all

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
import com.ierusalem.androchat.features.home.presentation.contacts.ContactItem
import com.ierusalem.androchat.features.home.presentation.contacts.ContactsScreen
import com.ierusalem.androchat.features.home.presentation.contacts.ContactsScreenData
import com.ierusalem.androchat.ui.components.ErrorScreen
import com.ierusalem.androchat.ui.components.LoadingScreen
import com.ierusalem.androchat.ui.theme.AndroChatTheme

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
                                    modifier = Modifier
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
            state =
//            ContactsScreen.Loading
            ContactsScreen.Success(
                listOf(
                    ContactsScreenData(
                        "Andro",
                        "hey, I am preparing a big surprise for you :)",
                        "09:20",
                        5
                    ),
                    ContactsScreenData(
                        "Jasur",
                        "hey, I am preparing a big surprise for you :)",
                        "11:20",
                        34
                    ),
                    ContactsScreenData(
                        "Sardor",
                        "hey, I am preparing a big surprise for you :)",
                        "12:24",
                        9
                    ),
                    ContactsScreenData(
                        "Ht-Nike",
                        "hey, I am preparing a big surprise for you :)",
                        "7:09",
                        1
                    ),
                    ContactsScreenData(
                        "Klea",
                        "hey, I am preparing a big surprise for you :)",
                        "9:21",
                        12
                    ),
                    ContactsScreenData(
                        "Andro",
                        "hey, I am preparing a big surprise for you :)",
                        "09:20",
                        5
                    ),
                    ContactsScreenData(
                        "Jasur",
                        "hey, I am preparing a big surprise for you :)",
                        "11:20",
                        34
                    ),
                    ContactsScreenData(
                        "Sardor",
                        "hey, I am preparing a big surprise for you :)",
                        "12:24",
                        9
                    ),
                    ContactsScreenData(
                        "Ht-Nike",
                        "hey, I am preparing a big surprise for you :)",
                        "7:09",
                        1
                    ),
                    ContactsScreenData(
                        "Klea",
                        "hey, I am preparing a big surprise for you :)",
                        "9:21",
                        12
                    ),
                    ContactsScreenData(
                        "Andro",
                        "hey, I am preparing a big surprise for you :)",
                        "09:20",
                        5
                    ),
                    ContactsScreenData(
                        "Jasur",
                        "hey, I am preparing a big surprise for you :)",
                        "11:20",
                        34
                    ),
                    ContactsScreenData(
                        "Sardor",
                        "hey, I am preparing a big surprise for you :)",
                        "12:24",
                        9
                    ),
                    ContactsScreenData(
                        "Ht-Nike",
                        "hey, I am preparing a big surprise for you :)",
                        "7:09",
                        1
                    ),
                    ContactsScreenData(
                        "Klea",
                        "hey, I am preparing a big surprise for you :)",
                        "9:21",
                        12
                    ),
                    ContactsScreenData(
                        "Ierusalem",
                        "haha, soon I will catch you all of you, haha be ready",
                        "10:12",
                        222
                    )
                )
            ),
        )
    }
}

@Preview
@Composable
fun AllChatsScreenScreenPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        AllChatsScreen(
            modifier = Modifier,
            state = ContactsScreen.Success(
                listOf(
                    ContactsScreenData(
                        "Andro",
                        "hey, I am preparing a big surprise for you :)",
                        "12:12",
                        2
                    ),
                    ContactsScreenData(
                        "Andro",
                        "hey, I am preparing a big surprise for you :)",
                        "02:34",
                        12
                    ),
                    ContactsScreenData(
                        "Andro",
                        "hey, I am preparing a big surprise for you :)",
                        "4: 08",
                        23
                    ),
                    ContactsScreenData(
                        "Andro",
                        "hey, I am preparing a big surprise for you :)",
                        "9:24",
                        1
                    ),
                    ContactsScreenData(
                        "Andro",
                        "hey, I am preparing a big surprise for you :)",
                        "18:23",
                        0,
                    ),
                    ContactsScreenData(
                        "Ierusalem",
                        "haha, soon I will catch you all of you",
                        "5:45",
                        290
                    )
                )
            ),
        )
    }
}
