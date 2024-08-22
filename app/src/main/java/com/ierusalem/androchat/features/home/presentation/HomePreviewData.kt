package com.ierusalem.androchat.features.home.presentation

import com.ierusalem.androchat.features.home.presentation.contacts.ContactsScreen
import com.ierusalem.androchat.features.home.presentation.contacts.ContactsScreenData
import com.ierusalem.androchat.features.home.presentation.contacts.ErrorType

@Suppress("unused")
object HomePreviewData {


    val tabItems = listOf("All", "Contacts", "Groups")
    val contactsLoading: ContactsScreen = ContactsScreen.Loading
    val contactsError: ContactsScreen = ContactsScreen.Error(ErrorType.InvalidResponse)
    val contactsSuccess = ContactsScreen.Success(
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
    )


}