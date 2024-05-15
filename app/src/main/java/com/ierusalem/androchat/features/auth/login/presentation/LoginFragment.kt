package com.ierusalem.androchat.features.auth.login.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.ierusalem.androchat.R
import com.ierusalem.androchat.features.auth.login.domain.LoginViewModel
import com.ierusalem.androchat.ui.theme.AndroChatTheme
import com.ierusalem.androchat.utils.executeWithLifecycle
import dagger.hilt.android.AndroidEntryPoint

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
                AndroChatTheme {
                    LoginScreen(
                        state = state,
                        intentReducer = { event -> viewModel.handleEvents(event) }
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
            LoginNavigation.ToHome -> {
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
            LoginNavigation.ToRegister -> {
                findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
            }
        }
    }

}