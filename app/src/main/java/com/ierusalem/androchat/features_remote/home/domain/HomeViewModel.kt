package com.ierusalem.androchat.features_remote.home.domain

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.connectivity.ConnectivityObserver
import com.ierusalem.androchat.core.ui.navigation.DefaultNavigationEventDelegate
import com.ierusalem.androchat.core.ui.navigation.NavigationEventDelegate
import com.ierusalem.androchat.core.ui.navigation.emitNavigation
import com.ierusalem.androchat.core.utils.UiText
import com.ierusalem.androchat.features_remote.home.presentation.HomeScreenClickIntents
import com.ierusalem.androchat.features_remote.home.presentation.HomeScreenNavigation
import com.ierusalem.androchat.features_remote.home.presentation.contacts.ContactsScreen
import com.ierusalem.androchat.features_remote.home.presentation.contacts.ContactsScreenData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver
) : ViewModel(),
    NavigationEventDelegate<HomeScreenNavigation> by DefaultNavigationEventDelegate() {

    init {
        connectivityObserver.observe().onEach { connectivityStatus ->
            when (connectivityStatus) {
                ConnectivityObserver.Status.Available -> {
                    _state.update {
                        it.copy(
                            connectivityStatus = UiText.StringResource(R.string.app_name)
                        )
                    }
                }

                ConnectivityObserver.Status.Loosing -> {
                    _state.update {
                        it.copy(
                            connectivityStatus = UiText.StringResource(R.string.connectivity_loosing)
                        )
                    }
                }

                ConnectivityObserver.Status.Lost -> {
                    _state.update {
                        it.copy(
                            connectivityStatus = UiText.StringResource(R.string.connectivity_lost)
                        )
                    }
                }

                ConnectivityObserver.Status.Unavailable -> {
                    _state.update {
                        it.copy(
                            connectivityStatus = UiText.StringResource(R.string.connectivity_unavailable)
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private val _state: MutableStateFlow<HomeScreenState> = MutableStateFlow(
        HomeScreenState()
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

            HomeScreenClickIntents.OnTcpClick -> {
                emitNavigation(HomeScreenNavigation.NavigateToTcp)
            }

            HomeScreenClickIntents.OnSearchClick -> {

            }

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

            HomeScreenClickIntents.ListItemClicked -> {
                emitNavigation(HomeScreenNavigation.NavigateToGroup)
            }
        }
    }

}

@Immutable
data class HomeScreenState(
    //tab row
    val tabItems: List<UiText> = listOf(
        UiText.StringResource(R.string.all),
        UiText.StringResource(R.string.contacts),
        UiText.StringResource(R.string.groups)
    ),
    val selectedTabIndex: Int = 0,
    //app bar
    val connectivityStatus: UiText = UiText.StringResource(R.string.connectivity_unavailable),

    //horizontal pager
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