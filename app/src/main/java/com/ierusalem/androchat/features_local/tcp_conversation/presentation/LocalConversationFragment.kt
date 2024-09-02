package com.ierusalem.androchat.features_local.tcp_conversation.presentation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.core.ui.components.PermissionDialog
import com.ierusalem.androchat.core.ui.components.ReadContactsPermissionTextProvider
import com.ierusalem.androchat.core.ui.components.RecordAudioPermissionTextProvider
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.Constants
import com.ierusalem.androchat.core.utils.Constants.getCurrentTime
import com.ierusalem.androchat.core.utils.executeWithLifecycle
import com.ierusalem.androchat.core.utils.generateFileFromUri
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.core.utils.makeCall
import com.ierusalem.androchat.core.utils.openAppSettings
import com.ierusalem.androchat.core.utils.openFile
import com.ierusalem.androchat.core.utils.readableFileSize
import com.ierusalem.androchat.features_local.tcp.domain.TcpViewModel
import com.ierusalem.androchat.features_local.tcp.domain.state.GeneralConnectionStatus
import com.ierusalem.androchat.features_local.tcp.presentation.TcpScreenEvents
import com.ierusalem.androchat.features_local.tcp.presentation.TcpScreenNavigation
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChatMessageEntity
import com.ierusalem.androchat.features_local.tcp.domain.state.FileMessageState
import com.ierusalem.androchat.features_local.tcp_conversation.presentation.components.ContactListContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class LocalConversationFragment : Fragment() {

    private val viewModel: TcpViewModel by activityViewModels()

    private lateinit var resourceDirectory: File

    //todo delegate this to visible_permission_dialogue
    private val readContactsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            viewModel.onPermissionResult(Manifest.permission.READ_CONTACTS, isGranted)
        }

    private val recordAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            viewModel.onPermissionResult(Manifest.permission.RECORD_AUDIO, isGranted)
            if (isGranted) {
                Toast.makeText(
                    requireContext(),
                    R.string.ready_to_record_voice_message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val getFilesLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_CANCELED -> {
                log("onActivityResult: RESULT CANCELED ")
            }

            Activity.RESULT_OK -> {
                val intent: Intent = result.data!!
                val uri = intent.data!!
                val file = generateFileFromUri(uri, resourceDirectory)

                val fileMessageEntity = ChatMessageEntity(
                    type = AppMessageType.FILE,
                    formattedTime = getCurrentTime(),
                    isFromYou = true,
                    userId = viewModel.state.value.peerUserUniqueId,

                    filePath = file.path,
                    fileState = FileMessageState.Loading(0),
                    fileName = file.name,
                    fileSize = file.length().readableFileSize(),
                    fileExtension = file.extension,
                )

                when (viewModel.state.value.generalConnectionStatus) {
                    GeneralConnectionStatus.Idle -> {
                        /** do nothing here */
                    }

                    GeneralConnectionStatus.ConnectedAsClient -> {
                        viewModel.sendClientMessage(fileMessageEntity)
                    }

                    GeneralConnectionStatus.ConnectedAsHost -> {
                        viewModel.sendHostMessage(fileMessageEntity)
                    }
                }
            }
        }
    }

    private fun showFileChooser() {
        val intent = Intent()
            .setType("*/*")
            .setAction(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.flags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        try {
            getFilesLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.please_install_a_file_manager),
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chattingUser = viewModel.state.value.currentChattingUser
        chattingUser?.let { user ->
            viewModel.loadMessages(user)
        }

        resourceDirectory = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS + "/${Constants.FOLDER_NAME_FOR_RESOURCES}"
        )!!
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {

                val uiState by viewModel.state.collectAsState()
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

                val visiblePermissionDialogQueue = viewModel.visiblePermissionDialogQueue

                AndroChatTheme {
                    if (uiState.showBottomSheet) {
                        //todo - delegate composable to file
                        ModalBottomSheet(
                            sheetState = sheetState,
                            onDismissRequest = {
                                viewModel.handleEvents(TcpScreenEvents.UpdateBottomSheetState(false))
                            },
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            content = {
                                if (uiState.isReadContactsGranted) {
                                    viewModel.handleEvents(TcpScreenEvents.ReadContacts)
                                    ContactListContent(
                                        contacts = uiState.contacts,
                                        shareSelectedContacts = { selectedContacts ->
                                            when (uiState.generalConnectionStatus) {
                                                GeneralConnectionStatus.Idle -> {
                                                    //do nothing
                                                }

                                                GeneralConnectionStatus.ConnectedAsClient -> {
                                                    lifecycleScope.launch(Dispatchers.IO) {
                                                        viewModel.handleEvents(
                                                            TcpScreenEvents.UpdateBottomSheetState(
                                                                false
                                                            )
                                                        )
                                                        selectedContacts.forEach { contact ->
                                                            val contactMessageEntity =
                                                                ChatMessageEntity(
                                                                    type = AppMessageType.CONTACT,
                                                                    formattedTime = getCurrentTime(),
                                                                    isFromYou = true,
                                                                    userId = viewModel.state.value.peerUserUniqueId,

                                                                    contactName = contact.contactName,
                                                                    contactNumber = contact.phoneNumber,
                                                                )
                                                            viewModel.sendClientMessage(contactMessageEntity)
                                                            delay(300)
                                                        }
                                                    }
                                                }

                                                GeneralConnectionStatus.ConnectedAsHost -> {
                                                    lifecycleScope.launch(Dispatchers.IO) {
                                                        viewModel.handleEvents(
                                                            TcpScreenEvents.UpdateBottomSheetState(
                                                                false
                                                            )
                                                        )
                                                        selectedContacts.forEach { contact ->
                                                            val contactMessageEntity =
                                                                ChatMessageEntity(
                                                                    type = AppMessageType.CONTACT,
                                                                    formattedTime = getCurrentTime(),
                                                                    isFromYou = true,
                                                                    userId = viewModel.state.value.peerUserUniqueId,

                                                                    contactName = contact.contactName,
                                                                    contactNumber = contact.phoneNumber,
                                                                )
                                                            viewModel.sendHostMessage(
                                                                contactMessageEntity
                                                            )
                                                            delay(300)
                                                        }
                                                    }
                                                }

                                            }
                                        }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .height(300.dp)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center,
                                        content = {
                                            Button(
                                                onClick = {
                                                    readContactsPermissionLauncher.launch(
                                                        Manifest.permission.READ_CONTACTS
                                                    )
                                                }
                                            ) {
                                                Text(text = stringResource(R.string.give_permission))
                                            }
                                        }
                                    )
                                }
                            }
                        )
                    }

                    visiblePermissionDialogQueue.reversed().forEach { permission ->
                        PermissionDialog(
                            permissionTextProvider = when (permission) {
                                Manifest.permission.READ_CONTACTS -> {
                                    ReadContactsPermissionTextProvider()
                                }

                                Manifest.permission.RECORD_AUDIO -> {
                                    RecordAudioPermissionTextProvider()
                                }

                                else -> return@forEach
                            },
                            isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                                permission
                            ),
                            onDismiss = viewModel::dismissPermissionDialog,
                            onOkClick = {
                                when (permission) {
                                    Manifest.permission.READ_CONTACTS -> {
                                        viewModel.dismissPermissionDialog()
                                        readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                    }

                                    Manifest.permission.RECORD_AUDIO -> {
                                        viewModel.dismissPermissionDialog()
                                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            onGoToAppSettingsClick = {
                                openAppSettings()
                                viewModel.dismissPermissionDialog()
                            }
                        )
                    }

                    ConversationContent(
                        uiState = uiState,
                        eventHandler = viewModel::handleEvents
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.checkReadContactsPermission()
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

            TcpScreenNavigation.ShowFileChooserClick -> {
                showFileChooser()
            }

            TcpScreenNavigation.RequestRecordAudioPermission -> {
                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            is TcpScreenNavigation.OnContactItemClick -> {
                makeCall(phoneNumber = navigation.message.contactNumber)
            }

            is TcpScreenNavigation.OnFileItemClick -> {
                openFile(
                    fileName = navigation.message.fileName,
                    resourceDirectory = resourceDirectory
                )
            }
        }
    }

}