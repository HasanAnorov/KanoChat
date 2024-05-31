package com.ierusalem.androchat.features_tcp.tcp_settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.ierusalem.androchat.ui.theme.AndroChatTheme

class TcpSettingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AndroChatTheme {
                    TcpSettingsScreen(
                        onNavIconClick = {
                            findNavController().popBackStack()
                        }
                    )
                }
            }
        }
    }

}