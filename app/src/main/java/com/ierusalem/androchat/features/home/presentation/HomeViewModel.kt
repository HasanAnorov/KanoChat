package com.ierusalem.androchat.features.home.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.ierusalem.androchat.features.home.presentation.contacts.ContactsScreen
import com.ierusalem.androchat.features.home.presentation.contacts.ContactsScreenData
import com.ierusalem.androchat.ui.navigation.DefaultNavigationEventDelegate
import com.ierusalem.androchat.ui.navigation.NavigationEventDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel(),
    NavigationEventDelegate<HomeScreenNavigation> by DefaultNavigationEventDelegate() {

    private val _state: MutableStateFlow<HomeScreenState> = MutableStateFlow(HomeScreenState())
    val state = _state.asStateFlow()

    private val _drawerShouldBeOpened = MutableStateFlow(false)
    val drawerShouldBeOpened = _drawerShouldBeOpened.asStateFlow()

    private fun openDrawer() {
        _drawerShouldBeOpened.value = true
    }

    fun resetOpenDrawerAction() {
        _drawerShouldBeOpened.value = false
    }

    fun handleClickIntents(intent: HomeScreenClickIntents) {
        when (intent) {
            is HomeScreenClickIntents.TabItemClicked -> {
                _state.update {
                    it.copy(
                        selectedTabIndex = intent.tabIndex
                    )
                }
            }
            HomeScreenClickIntents.NavIconClicked -> {
                openDrawer()
            }
        }
    }

}

@Immutable
data class HomeScreenState(
    val tabItems: List<String> = listOf("All", "Contacts", "Groups"),
    val selectedTabIndex: Int = 0,
//    val contacts: ContactsScreen = ContactsScreen.Loading,
//    val contacts: ContactsScreen = ContactsScreen.Error(ErrorType.InvalidResponse),
    val contacts: ContactsScreen = ContactsScreen.Success(
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
    ),
)