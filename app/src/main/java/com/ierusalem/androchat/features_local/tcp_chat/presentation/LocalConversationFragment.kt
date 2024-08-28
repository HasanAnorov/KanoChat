package com.ierusalem.androchat.features_local.tcp_chat.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.executeWithLifecycle
import com.ierusalem.androchat.features_local.tcp.domain.TcpViewModel
import com.ierusalem.androchat.features_local.tcp.presentation.utils.TcpScreenNavigation

class LocalConversationFragment : Fragment() {

    private val viewModel: TcpViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chattingUser = viewModel.state.value.currentChattingUser
        chattingUser?.let { user ->
            viewModel.loadMessages(user)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.state.collectAsState()

                AndroChatTheme {
                    ConversationContent(
                        uiState = uiState,
                        eventHandler = viewModel::handleEvents
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

    private fun executeNavigation(navigation: TcpScreenNavigation) {
        when (navigation) {
            TcpScreenNavigation.OnNavIconClick -> {
                findNavController().popBackStack()
            }
        }
    }

}