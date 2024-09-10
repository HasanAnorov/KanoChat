package com.ierusalem.androchat.features_local.tcp.presentation

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.components.CoarseLocationPermissionTextProvider
import com.ierusalem.androchat.core.ui.components.FineLocationPermissionTextProvider
import com.ierusalem.androchat.core.ui.components.NearbyWifiDevicesPermissionTextProvider
import com.ierusalem.androchat.core.ui.components.PermissionDialog
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.Constants
import com.ierusalem.androchat.core.utils.executeWithLifecycle
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.core.utils.openAppSettings
import com.ierusalem.androchat.core.utils.openWifiSettings
import com.ierusalem.androchat.core.utils.shortToast
import com.ierusalem.androchat.features_local.tcp.data.server.permission.PermissionGuardImpl
import com.ierusalem.androchat.features_local.tcp.domain.TcpViewModel
import com.ierusalem.androchat.features_local.tcp.presentation.components.rememberTcpAllTabs
import com.ierusalem.androchat.features_local.tcp.presentation.tcp_networking.components.ActionRequestDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TcpFragment : Fragment() {

    private val viewModel: TcpViewModel by activityViewModels()

    private lateinit var permissionGuard: PermissionGuardImpl

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                log("Precise location access granted: ")
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                log("Only approximate location access granted: ")
            }

            permissions.getOrDefault(Manifest.permission.NEARBY_WIFI_DEVICES, false) -> {
                // Only approximate location access granted.
                log("Only approximate location access granted: ")
            }

            else -> {
                // No location access granted.
                permissions.forEach { permission ->
                    viewModel.onPermissionResult(
                        permission = permission.key,
                        isGranted = permission.value
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        permissionGuard = PermissionGuardImpl(requireContext())

        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.state.collectAsStateWithLifecycle()

                val visibleActionDialogQueue = viewModel.visibleActionDialogQueue
                val visiblePermissionDialogQueue = viewModel.visiblePermissionDialogQueue

                val scope = rememberCoroutineScope()
                val allTabs = rememberTcpAllTabs()
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    initialPageOffsetFraction = 0F,
                    pageCount = { allTabs.size },
                )
                val focusManager = LocalFocusManager.current
                val handleTabSelected by rememberUpdatedState { tab: TcpView ->
                    // Click fires the index to update
                    // The index updating is caught by the snapshot flow
                    // Which then triggers the page update function
                    val index = allTabs.indexOf(tab)
                    scope.launch(context = Dispatchers.Main) {
                        pagerState.animateScrollToPage(
                            index
                        )
                    }
                }

                LaunchedEffect(key1 = pagerState.currentPage) {
                    focusManager.clearFocus()
                }

                AndroChatTheme {

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

                    visiblePermissionDialogQueue.reversed().forEach { permission ->
                        PermissionDialog(
                            permissionTextProvider = when (permission) {
                                Manifest.permission.NEARBY_WIFI_DEVICES -> {
                                    NearbyWifiDevicesPermissionTextProvider()
                                }

                                Manifest.permission.ACCESS_FINE_LOCATION -> {
                                    FineLocationPermissionTextProvider()
                                }

                                Manifest.permission.ACCESS_COARSE_LOCATION -> {
                                    CoarseLocationPermissionTextProvider()
                                }

                                else -> return@forEach
                            },
                            isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                                permission
                            ),
                            onDismiss = viewModel::dismissPermissionDialog,
                            onOkClick = {
                                viewModel.dismissPermissionDialog()

                                when (permission) {

                                    Manifest.permission.NEARBY_WIFI_DEVICES -> {
                                        locationPermissionRequest.launch(
                                            permissionGuard.requiredPermissionsForWifi.toTypedArray()
                                        )
                                    }

                                    Manifest.permission.ACCESS_FINE_LOCATION -> {
                                        locationPermissionRequest.launch(
                                            permissionGuard.requiredPermissionsForWifi.toTypedArray()
                                        )
                                    }

                                    Manifest.permission.ACCESS_COARSE_LOCATION -> {
                                        locationPermissionRequest.launch(
                                            permissionGuard.requiredPermissionsForWifi.toTypedArray()
                                        )
                                    }
                                }
                            },
                            onGoToAppSettingsClick = {
                                openAppSettings()
                                viewModel.dismissPermissionDialog()
                            }
                        )
                    }

                    TcpScreen(
                        uiState = uiState,
                        eventHandler = viewModel::handleEvents,
                        allTabs = allTabs,
                        pagerState = pagerState,
                        onTabChanged = { handleTabSelected(it) }
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

    override fun onDestroy() {
        super.onDestroy()
        log("onDestroy called")
    }

    private fun executeNavigation(navigation: TcpScreenNavigation) {
        when (navigation) {

            TcpScreenNavigation.RequestLocationPermission -> {
                locationPermissionRequest.launch(
                    permissionGuard.requiredPermissionsForWifi.toTypedArray()
                )
            }

            TcpScreenNavigation.WifiEnableRequest -> {
                openWifiSettings()
            }

            is TcpScreenNavigation.OnChattingUserClicked -> {
                val bundle = Bundle().apply {
                    putString(Constants.SELECTED_CHATTING_USER, navigation.selectUserStringForm)
                }
                findNavController().navigate(
                    R.id.action_tcpFragment_to_localConversationFragment,
                    bundle
                )
            }

            TcpScreenNavigation.OnSettingsClick -> {
                findNavController().navigate(R.id.action_tcpFragment_to_settingsFragment)
            }

            is TcpScreenNavigation.OnErrorsOccurred -> {
                shortToast(getString(navigation.tcpScreenErrors.errorMessage))
            }

        }
    }

}