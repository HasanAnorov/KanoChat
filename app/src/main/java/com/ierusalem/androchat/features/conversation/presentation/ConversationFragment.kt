package com.ierusalem.androchat.features.conversation.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.ierusalem.androchat.R
import com.ierusalem.androchat.data.exampleUiState
import com.ierusalem.androchat.features.conversation.domain.ConversationViewModel
import com.ierusalem.androchat.ui.theme.AndroChatTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * ConversationFragment
 *
 * @author A.H.I "andro" on 7/03/2024
 */

@AndroidEntryPoint
class ConversationFragment : Fragment() {

    private val viewModel: ConversationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(inflater.context).apply {
        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)

        setContent {
            AndroChatTheme {
                ConversationContent(
                    uiState = exampleUiState,
                    navigateToProfile = { user ->
                        val bundle = bundleOf("userId" to user)
                        findNavController().navigate(
                            R.id.profileFragment,
                            bundle
                        )
                    },
                    onNavIconPressed = { findNavController().popBackStack() }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.connectToChat()
    }

    override fun onStop() {
        super.onStop()
        viewModel.disconnect()
    }

}
