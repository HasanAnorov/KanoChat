package com.ierusalem.androchat.features_remote.conversation.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.ierusalem.androchat.core.constants.Constants
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.executeWithLifecycle
import com.ierusalem.androchat.features_remote.conversation.domain.ConversationNavigation
import com.ierusalem.androchat.features_remote.conversation.domain.ConversationViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * ConversationFragment
 *
 * @author A.H.I "andro" on 7/03/2024
 */

@AndroidEntryPoint
class ConversationFragment : Fragment() {

    private val viewModel: ConversationViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString(Constants.USERNAME_REGISTER_TO_HOME)?.let { username ->
            Log.d("ahi3646", "onAttach: $username ")
            viewModel.connectToChat(username)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(inflater.context).apply {
        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)

        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            AndroChatTheme {
                ConversationContent(
                    uiState = state,
                    intentReducer = {event -> viewModel.handleIntents(event)}
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

    private fun executeNavigation(navigation: ConversationNavigation) {
        when (navigation) {
            ConversationNavigation.OnNavIconClick -> {
                findNavController().popBackStack()
            }
        }
    }

}
