package com.ierusalem.androchat.features.auth.register.presentation

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
import com.ierusalem.androchat.features.auth.register.domain.RegistrationViewModel
import com.ierusalem.androchat.ui.theme.AndroChatTheme
import com.ierusalem.androchat.utils.executeWithLifecycle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistrationFragment : Fragment() {

    private val viewModel: RegistrationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.state.collectAsStateWithLifecycle()
                AndroChatTheme {
                    RegistrationScreen(
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

    private fun executeNavigation(navigation: RegistrationNavigation) {
        when (navigation) {
            is RegistrationNavigation.ToHome -> {
//                val bundle = bundleOf(Constants.USERNAME_REGISTER_TO_HOME to navigation.username)
//                findNavController().navigate(
//                    R.id.action_registrationFragment_to_conversationFragment,
//                    bundle
//                )
                findNavController().navigate(R.id.action_registrationFragment_to_homeFragment)
            }

            RegistrationNavigation.ToLogin -> {
                findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
            }
        }
    }

}