package com.ierusalem.androchat.features.auth.register.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.ierusalem.androchat.features.auth.register.domain.RegistrationViewModel
import com.ierusalem.androchat.features.auth.register.domain.use_case.ValidatorUseCase

class RegistrationFragment : Fragment() {

    private val viewModel: RegistrationViewModel = RegistrationViewModel(
        ValidatorUseCase()
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {

        }
    }

}