package com.ierusalem.androchat.features.auth.login.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ierusalem.androchat.features.auth.login.domain.LoginViewModel
import com.ierusalem.androchat.utils.executeWithLifecycle

class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel = LoginViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.state.collectAsStateWithLifecycle()
                LoginScreen(
                    state = state,
                    intentReducer = { event -> viewModel.handleEvents(event) }
                )
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
            LoginNavigation.ToHome -> {}
            LoginNavigation.ToRegister -> {

            }
        }
    }

}