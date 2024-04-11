package com.ierusalem.androchat.features.home.domain

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.ierusalem.androchat.app.AppTheme
import com.ierusalem.androchat.features.home.presentation.HomeScreenClickIntents
import com.ierusalem.androchat.features.home.presentation.HomeScreenNavigation
import com.ierusalem.androchat.features.home.presentation.contacts.ContactsScreen
import com.ierusalem.androchat.features.home.presentation.contacts.ErrorType
import com.ierusalem.androchat.ui.navigation.DefaultNavigationEventDelegate
import com.ierusalem.androchat.ui.navigation.NavigationEventDelegate
import com.ierusalem.androchat.ui.navigation.emitNavigation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(repo: HomeRepository) : ViewModel(),
    NavigationEventDelegate<HomeScreenNavigation> by DefaultNavigationEventDelegate() {

    private val _state: MutableStateFlow<HomeScreenState> = MutableStateFlow(
        HomeScreenState(
            appTheme = repo.getAppTheme()
        )
    )
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
            HomeScreenClickIntents.DrawerSettingClick -> {
                emitNavigation(HomeScreenNavigation.NavigateToSettings)
            }
            is HomeScreenClickIntents.TabItemClicked -> {
                _state.update {
                    it.copy(
                        selectedTabIndex = intent.tabIndex
                    )
                }
            }
            HomeScreenClickIntents.NavIconClicked -> { openDrawer() }
            HomeScreenClickIntents.ListItemClicked -> {
                emitNavigation(HomeScreenNavigation.NavigateToGroup)
            }
        }
    }

}

@Immutable
data class HomeScreenState(
    val tabItems: List<String> = listOf("All", "Contacts", "Groups"),
    val selectedTabIndex: Int = 0,
    val appTheme: AppTheme,
//    val contacts: ContactsScreen = ContactsScreen.Loading,
    val contacts: ContactsScreen = ContactsScreen.Error(ErrorType.InvalidResponse),
//    val contacts: ContactsScreen = ContactsScreen.Success(
//        listOf(
//            ContactsScreenData(
//                "Andro",
//                "hey, I am preparing a big surprise for you :)",
//                "12:12",
//                2
//            ),
//            ContactsScreenData(
//                "Andro",
//                "hey, I am preparing a big surprise for you :)",
//                "02:34",
//                12
//            ),
//            ContactsScreenData(
//                "Andro",
//                "hey, I am preparing a big surprise for you :)",
//                "4: 08",
//                23
//            ),
//            ContactsScreenData(
//                "Andro",
//                "hey, I am preparing a big surprise for you :)",
//                "9:24",
//                1
//            ),
//            ContactsScreenData(
//                "Andro",
//                "hey, I am preparing a big surprise for you :)",
//                "18:23",
//                0,
//            ),
//            ContactsScreenData(
//                "Andro",
//                "hey, I am preparing a big surprise for you :)",
//                "02:34",
//                12
//            ),
//            ContactsScreenData(
//                "Andro",
//                "hey, I am preparing a big surprise for you :)",
//                "4: 08",
//                23
//            ),
//            ContactsScreenData(
//                "Andro",
//                "hey, I am preparing a big surprise for you :)",
//                "9:24",
//                1
//            ),
//            ContactsScreenData(
//                "Andro",
//                "hey, I am preparing a big surprise for you :)",
//                "18:23",
//                0,
//            ),
//            ContactsScreenData(
//                "Andro",
//                "hey, I am preparing a big surprise for you :)",
//                "02:34",
//                12
//            ),
//            ContactsScreenData(
//                "Andro",
//                "hey, I am preparing a big surprise for you :)",
//                "4: 08",
//                23
//            ),
//            ContactsScreenData(
//                "Andro",
//                "hey, I am preparing a big surprise for you :)",
//                "9:24",
//                1
//            ),
//            ContactsScreenData(
//                "Andro",
//                "hey, I am preparing a big surprise for you :)",
//                "18:23",
//                0,
//            ),
//            ContactsScreenData(
//                "Ierusalem",
//                "haha, soon I will catch you all of you",
//                "5:45",
//                290
//            )
//        )
//    ),
)