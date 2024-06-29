package com.ierusalem.androchat.features_tcp.tcp_chat.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.components.EmptyScreen
import com.ierusalem.androchat.core.ui.components.ErrorScreen
import com.ierusalem.androchat.core.ui.components.LoadingScreen
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.Resource
import com.ierusalem.androchat.features.home.presentation.contacts.ErrorType
import com.ierusalem.androchat.features_tcp.tcp.domain.state.ContactItem

@Composable
fun ContactListContent(
    modifier: Modifier = Modifier,
    contacts: Resource<List<ContactItem>>,
    shareSelectedContacts: (List<ContactItem>) -> Unit
) {
    val lazyListState = rememberLazyListState()
    when (contacts) {
        is Resource.Loading -> {
            LoadingScreen(modifier = modifier)
        }

        is Resource.Success -> {
            val data = contacts.data!!
            if (data.isEmpty()) {
                EmptyScreen()
            } else {
                //i don't know but in real testing this reduces the lagging of lazyColumn maybe
                // it's because of derived state behaviour
                val dContacts by remember {
                    derivedStateOf {
                        data.toMutableStateList()
                    }
                }

                Column {
                    if (dContacts.any { it.isSelected }) {
                        val selectedContacts = dContacts.filter { it.isSelected }
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .padding(bottom = 8.dp, top = 8.dp)
                                .clickable { shareSelectedContacts(selectedContacts) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Share",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = if (selectedContacts.size == 1) "contact" else "( ${selectedContacts.size} contacts )",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.weight(1F))
                                Icon(
                                    modifier = Modifier.padding(end = 8.dp),
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    painter = painterResource(id = R.drawable.send),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        itemsIndexed(
                            items = dContacts,
                            key = { _, item -> item.id }
                        ) { index, contactItem ->
                            ContactItemContent(
                                contactItem = contactItem,
                                onItemCLick = {
                                    dContacts[index] = dContacts[index].copy(
                                        isSelected = !contactItem.isSelected
                                    )
                                }
                            )
                            if (index < data.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
        }

        is Resource.Failure -> {
            ErrorScreen(error = ErrorType.NetworkError) {

            }
        }
    }
}

@Preview
@Composable
private fun PreviewContactListContent() {
    AndroChatTheme {
        Surface {
            ContactListContent(
                contacts = Resource.Loading(),
                shareSelectedContacts = {}
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDarkContactListContent() {
    AndroChatTheme(isDarkTheme = true) {
        Surface {
            ContactListContent(
                contacts = Resource.Success(
                    listOf(
                        ContactItem("Hasan", "93 3373646", false),
                        ContactItem("Hasan", "93 3373646", false),
                        ContactItem("Hasan", "93 3373646", false),
                        ContactItem("Hasan", "93 3373646", true),
                        ContactItem("Hasan", "93 3373646", false),
                        ContactItem("Hasan", "93 3373646", false),
                        ContactItem("Hasan", "93 3373646", true),
                        ContactItem("Hasan", "93 3373646", false),
                        ContactItem("Hasan", "93 3373646", true),
                        ContactItem("Hasan", "93 3373646", false),
                        ContactItem("Hasan", "93 3373646", false),
                        ContactItem("Hasan", "93 3373646", false),
                        ContactItem("Hasan", "93 3373646", false),
                    )
                ),
                shareSelectedContacts = {}
            )
        }
    }
}