package com.ierusalem.androchat.features_remote.home.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.ierusalem.androchat.features_remote.home.domain.HomeScreenState
import com.ierusalem.androchat.features_remote.home.presentation.all.AllChatsScreen
import com.ierusalem.androchat.features_remote.home.presentation.contacts.ContactsScreen
import com.ierusalem.androchat.features_remote.home.presentation.contacts.ErrorType
import com.ierusalem.androchat.features_remote.home.presentation.group.GroupScreen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    state: HomeScreenState,
    pagerState: PagerState,
    allTabs: SnapshotStateList<HomeView>,
    eventHandler: (HomeScreenClickIntents) -> Unit,
) {
    HorizontalPager(
        modifier = modifier,
        state = pagerState,
    ) { page ->
        val screen = remember(
            allTabs,
            page,
        ) {
            allTabs[page]
        }
        when (screen) {
            HomeView.All -> AllChatsScreen(
                state = HomePreviewData.contactsSuccess
            )

            HomeView.Contacts -> ContactsScreen(
                state = state.contacts,
                intentReducer = eventHandler
            )

            HomeView.Groups -> GroupScreen(state = ContactsScreen.Error(ErrorType.NetworkError))
        }
    }
}