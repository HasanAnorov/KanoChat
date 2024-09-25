package com.ierusalem.androchat.features_common.settings.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.executeWithLifecycle
import com.ierusalem.androchat.features_common.settings.domain.SettingsViewModel
import com.ierusalem.androchat.features_local.tcp.domain.TcpViewModel
import com.ierusalem.androchat.features_local.tcp.presentation.tcp_networking.components.ActionRequestDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val activityViewModel: TcpViewModel by activityViewModels()
    private val viewModel: SettingsViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        viewModel.initLanguageAndTheme()
        viewModel.initBroadcastFrequency()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {

                val uiState by viewModel.state.collectAsStateWithLifecycle()
                val visibleActionDialogQueue = viewModel.visibleActionDialogQueue

                AndroChatTheme(isDarkTheme = uiState.appTheme) {
                    visibleActionDialogQueue.forEach { actionDialog ->
                        ActionRequestDialog(
                            onDismissRequest = actionDialog.onNegativeButtonClick,
                            onConfirmation = actionDialog.onPositiveButtonClick,
                            dialogTitle = actionDialog.dialogTitle,
                            dialogText = actionDialog.dialogMessage,
                            icon = actionDialog.icon,
                            positiveButtonRes = actionDialog.positiveButtonText,
                            negativeButtonRes = actionDialog.negativeButtonText
                        )
                    }
                    SettingsScreen(
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

    private fun executeNavigation(navigation: SettingsScreenNavigation) {
        when (navigation) {
            SettingsScreenNavigation.NavIconClick -> {
                findNavController().popBackStack()
            }

            SettingsScreenNavigation.ToLogin -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    activityViewModel.logout()
                }
                findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToLoginFragment())
            }
        }
    }

}