package com.ierusalem.androchat.features.home.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.ierusalem.androchat.R
import com.ierusalem.androchat.features.home.domain.HomeViewModel
import com.ierusalem.androchat.features.home.presentation.components.rememberHomeAllTabs
import com.ierusalem.androchat.ui.components.AndroChatDrawer
import com.ierusalem.androchat.ui.theme.AndroChatTheme
import com.ierusalem.androchat.utils.executeWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {

                val state by viewModel.state.collectAsStateWithLifecycle()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val drawerOpen by viewModel.drawerShouldBeOpened
                    .collectAsStateWithLifecycle()

                val scope = rememberCoroutineScope()
                val allTabs = rememberHomeAllTabs()
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    initialPageOffsetFraction = 0F,
                    pageCount = { allTabs.size },
                )

                val handleTabSelected by rememberUpdatedState { tab: HomeView ->
                    // Click fires the index to update
                    // The index updating is caught by the snapshot flow
                    // Which then triggers the page update function
                    val index = allTabs.indexOf(tab)
                    scope.launch(context = Dispatchers.Main) {
                        pagerState.animateScrollToPage(
                            index
                        )
                    }
                }

                if (drawerOpen) {
                    // Open drawer and reset state in VM.
                    LaunchedEffect(Unit) {
                        // wrap in try-finally to handle interruption whiles opening drawer
                        try {
                            drawerState.open()
                        } finally {
                            viewModel.resetOpenDrawerAction()
                        }
                    }
                }

                // Intercepts back navigation when the drawer is open
                if (drawerState.isOpen) {
                    BackHandler {
                        scope.launch {
                            drawerState.close()
                        }
                    }
                }

                AndroChatTheme(isDarkTheme = true) {
                    AndroChatDrawer(
                        drawerState = drawerState,
                        onDrawerItemClick = {
                            scope.launch {
                                drawerState.close()
                                viewModel.handleClickIntents(it)
                            }
                        },
                        content = {
                            HomeScreen(
                                state = state,
                                allTabs = allTabs,
                                pagerState = pagerState,
                                eventHandler = {
                                    viewModel.handleClickIntents(it)
                                },
                                onTabChanged = {
                                    handleTabSelected(it)
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.screenNavigation.executeWithLifecycle(
            lifecycle = viewLifecycleOwner.lifecycle,
            action = ::executeNavigation
        )
    }

    private fun executeNavigation(navigation: HomeScreenNavigation) {
        when (navigation) {

            HomeScreenNavigation.NavigateToPrivate -> {

            }

            HomeScreenNavigation.NavigateToSettings -> {
                findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
            }

            HomeScreenNavigation.NavigateToGroup -> {
                findNavController().navigate(R.id.action_homeFragment_to_conversationFragment)
            }

            HomeScreenNavigation.NavigateToTcp -> {
                findNavController().navigate(R.id.action_homeFragment_to_tcpFragment)
            }
        }
    }

}