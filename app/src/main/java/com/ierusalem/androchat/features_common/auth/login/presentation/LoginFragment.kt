package com.ierusalem.androchat.features_common.auth.login.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.executeWithLifecycle
import com.ierusalem.androchat.features_common.auth.login.domain.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.state.collectAsStateWithLifecycle()
                val visibeSnackbarMessages = viewModel.visibleSnackbarMessagesQueue

                val snackbarHostState = remember {
                    SnackbarHostState()
                }

                AndroChatTheme {
                    val scope = rememberCoroutineScope()

                    visibeSnackbarMessages.forEach { message ->
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = getString(message.message),
                                actionLabel = getString(message.actionLabel),
                                duration = SnackbarDuration.Short,
                            )
                            when (result) {
                                SnackbarResult.ActionPerformed -> {
                                    viewModel.visibleSnackbarMessagesQueue.clear()
                                }

                                SnackbarResult.Dismissed -> {
                                    viewModel.visibleSnackbarMessagesQueue.clear()
                                }
                            }
                        }
                    }

                    LoginScreen(
                        state = state,
                        intentReducer = { event -> viewModel.handleEvents(event) },
                        snackbarHostState = snackbarHostState
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

    private fun executeNavigation(navigation: LoginNavigation) {
        when (navigation) {
            LoginNavigation.ToLocal -> {
                findNavController().navigate(R.id.action_loginFragment_to_tcpFragment)
            }
        }
    }

}