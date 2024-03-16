package com.ierusalem.androchat.features.home.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.findNavController
import com.ierusalem.androchat.R
import com.ierusalem.androchat.ui.components.AndroChatDrawer
import com.ierusalem.androchat.ui.theme.AndroChatTheme
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel = HomeViewModel()

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
                val scope = rememberCoroutineScope()
                if (drawerState.isOpen) {
                    BackHandler {
                        scope.launch {
                            drawerState.close()
                        }
                    }
                }

                AndroChatTheme {
                    AndroChatDrawer(
                        drawerState = drawerState,
                        onChatClicked = {
//                            val bundle = bundleOf("userId" to it)
//                            findNavController().navigate(R.id.profileFragment, bundle)
//                            scope.launch {
//                                drawerState.close()
//                            }
                            findNavController().popBackStack(R.id.conversationFragment, false)
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        content = {
                            HomeScreen(
                                state = state,
                                intentReducer = {intent ->
                                    viewModel.handleClickIntents(intent)
                                }
                            )
                        }
                    )
                }
            }
        }
    }

}