package com.ierusalem.androchat.features_tcp.tcp_chat.presentation

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.InsertPhoto
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.devlomi.record_view.OnRecordListener
import com.devlomi.record_view.RecordButton
import com.devlomi.record_view.RecordLockView
import com.devlomi.record_view.RecordView
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.features_tcp.tcp.domain.state.GeneralConnectionStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.state.TcpScreenUiState
import com.ierusalem.androchat.features_tcp.tcp.presentation.utils.TcpScreenEvents

enum class LocalInputSelector {
    NONE,
    EMOJI,
    PHONE,
    PICTURE,
    FILE
}

@Preview
@Composable
fun UserInputPreview() {
    LocalConversationUserInput(
        eventHandler = {},
        uiState = TcpScreenUiState()
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalConversationUserInput(
    eventHandler: (TcpScreenEvents) -> Unit,
    modifier: Modifier = Modifier,
    resetScroll: () -> Unit = {},
    uiState: TcpScreenUiState
) {
    var currentInputSelector by rememberSaveable { mutableStateOf(LocalInputSelector.NONE) }
    val dismissKeyboard = { currentInputSelector = LocalInputSelector.NONE }

    // Intercept back navigation if there's a InputSelector visible
    if (currentInputSelector != LocalInputSelector.NONE) {
        BackHandler(onBack = dismissKeyboard)
    }

    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    // Used to decide if the keyboard should be shown
    var textFieldFocusState by remember { mutableStateOf(false) }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { medias ->
            eventHandler(TcpScreenEvents.HandlePickingMultipleMedia(medias))
            dismissKeyboard()
        }
    )

    Surface(
        tonalElevation = 2.dp,
        contentColor = MaterialTheme.colorScheme.secondary
    ) {
        Column(modifier = modifier) {
            Box(contentAlignment = Alignment.BottomStart) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth(),
                    factory = { context ->
                        val parent = FrameLayout(context)
                        LayoutInflater.from(context).inflate(R.layout.recording_view, parent, false)
                            .apply {
                                val recordingView = findViewById<RecordView>(R.id.record_view)
                                val recordButton = findViewById<RecordButton>(R.id.record_button)

                                val isEnabled =
                                    uiState.generalConnectionStatus != GeneralConnectionStatus.Idle
                                recordButton.isEnabled = isEnabled

                                val recordLockView = findViewById<RecordLockView>(R.id.record_lock)
                                val recordLockCoverView =
                                    findViewById<ImageView>(R.id.record_lock_cover)
                                recordLockView.visibility = View.INVISIBLE
                                recordButton.setRecordView(recordingView)
                                recordingView.setOnRecordListener(
                                    object : OnRecordListener {
                                        override fun onStart() {
                                            //Start Recording..
                                            log("onStart")
                                            eventHandler(TcpScreenEvents.OnVoiceRecordStart)
                                            recordLockCoverView.visibility = View.VISIBLE
                                        }

                                        override fun onCancel() {
                                            //On Swipe To Cancel
                                            log("onCancel")
                                            eventHandler(TcpScreenEvents.OnVoiceRecordCancelled)
                                            recordLockCoverView.visibility = View.INVISIBLE
                                        }

                                        override fun onFinish(
                                            recordTime: Long,
                                            limitReached: Boolean
                                        ) {
                                            //Stop Recording..
                                            //limitReached to determine if the Record
                                            // was finished when time limit reached.
                                            log("onFinish")
                                            eventHandler(TcpScreenEvents.OnVoiceRecordFinished)
                                            recordLockCoverView.visibility = View.INVISIBLE

                                        }

                                        override fun onLessThanSecond() {
                                            //When the record time is less than One Second
                                            log("onLessThanSecond")
                                            eventHandler(TcpScreenEvents.OnVoiceRecordCancelled)
                                            recordLockCoverView.visibility = View.INVISIBLE
                                        }

                                        override fun onLock() {
                                            //When Lock gets activated
                                            log("onLock")
                                            recordLockCoverView.visibility = View.INVISIBLE
                                        }
                                    }
                                )

                                recordingView.setLockEnabled(true)
                                recordingView.setRecordLockImageView(recordLockView)

                                // prevent recording under one Second (it's false by default)
                                recordingView.setLessThanSecondAllowed(false)

                                // enable or disable the Growing animation for record Button.
                                recordingView.setRecordButtonGrowingAnimationEnabled(true)

                                // change scale up value on Growing animation.
                                recordButton.setScaleUpTo(1.5f)

                                // disable Sounds
                                recordingView.setSoundEnabled(true)

                                // auto cancelling recording after timeLimit (In millis)
                                recordingView.setTimeLimit(5 * 60 * 1000) //5 minutes

                                //set send icon
                                recordButton.setSendIconResource(R.drawable.send)
                            }
                    },
                    update = {

                    }
                )
                if (!uiState.isRecording) {
                    UserInputText(
                        textFieldValue = textState,
                        onTextChanged = { textState = it },
                        // Only show the keyboard if there's no input selector and text field has focus
                        keyboardShown = currentInputSelector == LocalInputSelector.NONE && textFieldFocusState,
                        // Close extended selector if text field receives focus
                        onTextFieldFocused = { focused ->
                            if (focused) {
                                currentInputSelector = LocalInputSelector.NONE
                                resetScroll()
                            }
                            textFieldFocusState = focused
                        },
                        focusState = textFieldFocusState
                    )
                }
            }
            UserInputSelector(
                onSelectorChange = {
                    when (it) {
                        LocalInputSelector.EMOJI, LocalInputSelector.NONE -> {
                            currentInputSelector = it
                        }

                        LocalInputSelector.FILE -> {
                            eventHandler(TcpScreenEvents.ShowFileChooserClick)
                        }

                        LocalInputSelector.PHONE -> {
                            dismissKeyboard()
                            eventHandler(TcpScreenEvents.UpdateBottomSheetState(true))
                        }

                        LocalInputSelector.PICTURE -> {
                            multiplePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        }
                    }
                },
                sendMessageEnabled = textState.text.isNotBlank(),
                onMessageSent = {
                    eventHandler(TcpScreenEvents.SendMessageRequest(textState.text))
                    // Reset text field and close keyboard
                    textState = TextFieldValue()
                    // Move scroll to bottom
                    resetScroll()
                    dismissKeyboard()
                },
                currentInputSelector = currentInputSelector
            )
            SelectorExpanded(
                onTextAdded = { textState = textState.addText(it) },
                currentSelector = currentInputSelector,
            )
        }
    }
}

private fun TextFieldValue.addText(newString: String): TextFieldValue {
    val newText = this.text.replaceRange(
        this.selection.start,
        this.selection.end,
        newString
    )
    val newSelection = TextRange(
        start = newText.length,
        end = newText.length
    )

    return this.copy(text = newText, selection = newSelection)
}

@Composable
private fun SelectorExpanded(
    currentSelector: LocalInputSelector,
    onTextAdded: (String) -> Unit,
) {
    if (currentSelector == LocalInputSelector.FILE) return

    // Request focus to force the TextField to lose it
    val focusRequester = FocusRequester()
    // If the selector is shown, always request focus to trigger a TextField.onFocusChange.
    SideEffect {
        if (currentSelector == LocalInputSelector.EMOJI) {
            focusRequester.requestFocus()
        }
    }

    Surface(tonalElevation = 8.dp) {
        when (currentSelector) {
            LocalInputSelector.EMOJI -> EmojiSelector(onTextAdded, focusRequester)
            else -> {}
        }
    }
}

@Composable
private fun UserInputSelector(
    onSelectorChange: (LocalInputSelector) -> Unit,
    sendMessageEnabled: Boolean,
    onMessageSent: () -> Unit,
    currentInputSelector: LocalInputSelector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(72.dp)
            .wrapContentHeight()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InputSelectorButton(
            onClick = { onSelectorChange(LocalInputSelector.EMOJI) },
            icon = Icons.Outlined.Mood,
            selected = currentInputSelector == LocalInputSelector.EMOJI,
            description = stringResource(id = R.string.emoji_selector_bt_desc)
        )
        InputSelectorButton(
            onClick = { onSelectorChange(LocalInputSelector.PICTURE) },
            icon = Icons.Outlined.InsertPhoto,
            selected = false,
            description = stringResource(id = R.string.attach_photo_desc)
        )
        InputSelectorButton(
            onClick = { onSelectorChange(LocalInputSelector.PHONE) },
            icon = Icons.Outlined.Call,
            selected = false,
            description = stringResource(id = R.string.videochat_desc)
        )
        InputSelectorButton(
            onClick = {
                onSelectorChange(LocalInputSelector.FILE)
            },
            icon = Icons.Outlined.AttachFile,
            selected = false,
            description = stringResource(id = R.string.videochat_desc)
        )

        val border = if (!sendMessageEnabled) {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        } else {
            null
        }
        Spacer(modifier = Modifier.weight(1f))

        val disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

        val buttonColors = ButtonDefaults.buttonColors(
            disabledContainerColor = Color.Transparent,
            disabledContentColor = disabledContentColor
        )

        // Send button
        Button(
            modifier = Modifier.height(36.dp),
            enabled = sendMessageEnabled,
            onClick = onMessageSent,
            colors = buttonColors,
            border = border,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                stringResource(id = R.string.send),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun InputSelectorButton(
    onClick: () -> Unit,
    icon: ImageVector,
    description: String,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundModifier = if (selected) {
        Modifier.background(
            color = LocalContentColor.current,
            shape = RoundedCornerShape(14.dp)
        )
    } else {
        Modifier
    }
    IconButton(
        onClick = onClick,
        modifier = modifier.then(backgroundModifier)
    ) {
        val tint = if (selected) {
            contentColorFor(backgroundColor = LocalContentColor.current)
        } else {
            LocalContentColor.current
        }
        Icon(
            icon,
            tint = tint,
            modifier = Modifier
                .padding(8.dp)
                .size(56.dp),
            contentDescription = description
        )
    }
}

@Preview
@Composable
private fun PreviewInputSelectorButton() {
    AndroChatTheme {

    }
}

val KeyboardShownKey = SemanticsPropertyKey<Boolean>("KeyboardShownKey")
var SemanticsPropertyReceiver.keyboardShownProperty by KeyboardShownKey

@ExperimentalFoundationApi
@Composable
private fun UserInputText(
    keyboardType: KeyboardType = KeyboardType.Text,
    onTextChanged: (TextFieldValue) -> Unit,
    textFieldValue: TextFieldValue,
    keyboardShown: Boolean,
    onTextFieldFocused: (Boolean) -> Unit,
    focusState: Boolean
) {
    val a11ylabel = stringResource(id = R.string.textfield_desc)
    Box(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(end = 64.dp)
    ) {
        UserInputTextField(
            textFieldValue,
            onTextChanged,
            onTextFieldFocused,
            keyboardType,
            focusState,
            Modifier.semantics {
                contentDescription = a11ylabel
                keyboardShownProperty = keyboardShown
            }
        )
    }
}

@Composable
private fun BoxScope.UserInputTextField(
    textFieldValue: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    onTextFieldFocused: (Boolean) -> Unit,
    keyboardType: KeyboardType,
    focusState: Boolean,
    modifier: Modifier = Modifier
) {
    var lastFocusState by remember { mutableStateOf(false) }
    BasicTextField(
        value = textFieldValue,
        onValueChange = { onTextChanged(it) },
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 16.dp)
            .align(Alignment.CenterStart)
            .onFocusChanged { state ->
                if (lastFocusState != state.isFocused) {
                    onTextFieldFocused(state.isFocused)
                }
                lastFocusState = state.isFocused
            },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Send
        ),
        maxLines = 4,
        cursorBrush = SolidColor(LocalContentColor.current),
        textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current)
    )

    val disableContentColor =
        MaterialTheme.colorScheme.onSurfaceVariant
    if (textFieldValue.text.isEmpty() && !focusState) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 32.dp),
            text = stringResource(R.string.textfield_hint),
            style = MaterialTheme.typography.bodyLarge.copy(color = disableContentColor)
        )
    }
}


@Composable
fun EmojiSelector(
    onTextAdded: (String) -> Unit,
    focusRequester: FocusRequester
) {

    val a11yLabel = stringResource(id = R.string.emoji_selector_desc)
    Column(
        modifier = Modifier
            .focusRequester(focusRequester) // Requests focus when the Emoji selector is displayed
            // Make the emoji selector focusable so it can steal focus from TextField
            .focusTarget()
            .semantics { contentDescription = a11yLabel }
    ) {
        Row(modifier = Modifier.verticalScroll(rememberScrollState())) {
            EmojiTable(onTextAdded, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun EmojiTable(
    onTextAdded: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        repeat(4) { x ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(EMOJI_COLUMNS) { y ->
                    val emoji = emojis[x * EMOJI_COLUMNS + y]
                    Text(
                        modifier = Modifier
                            .clickable(onClick = { onTextAdded(emoji) })
                            .sizeIn(minWidth = 42.dp, minHeight = 42.dp)
                            .padding(8.dp),
                        text = emoji,
                        style = LocalTextStyle.current.copy(
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}

private const val EMOJI_COLUMNS = 10

private val emojis = listOf(
    "\ud83d\ude00", // Grinning Face
    "\ud83d\ude01", // Grinning Face With Smiling Eyes
    "\ud83d\ude02", // Face With Tears of Joy
    "\ud83d\ude03", // Smiling Face With Open Mouth
    "\ud83d\ude04", // Smiling Face With Open Mouth and Smiling Eyes
    "\ud83d\ude05", // Smiling Face With Open Mouth and Cold Sweat
    "\ud83d\ude06", // Smiling Face With Open Mouth and Tightly-Closed Eyes
    "\ud83d\ude09", // Winking Face
    "\ud83d\ude0a", // Smiling Face With Smiling Eyes
    "\ud83d\ude0b", // Face Savouring Delicious Food
    "\ud83d\ude0e", // Smiling Face With Sunglasses
    "\ud83d\ude0d", // Smiling Face With Heart-Shaped Eyes
    "\ud83d\ude18", // Face Throwing a Kiss
    "\ud83d\ude17", // Kissing Face
    "\ud83d\ude19", // Kissing Face With Smiling Eyes
    "\ud83d\ude1a", // Kissing Face With Closed Eyes
    "\u263a", // White Smiling Face
    "\ud83d\ude42", // Slightly Smiling Face
    "\ud83e\udd17", // Hugging Face
    "\ud83d\ude07", // Smiling Face With Halo
    "\ud83e\udd13", // Nerd Face
    "\ud83e\udd14", // Thinking Face
    "\ud83d\ude10", // Neutral Face
    "\ud83d\ude11", // Expressionless Face
    "\ud83d\ude36", // Face Without Mouth
    "\ud83d\ude44", // Face With Rolling Eyes
    "\ud83d\ude0f", // Smirking Face
    "\ud83d\ude23", // Persevering Face
    "\ud83d\ude25", // Disappointed but Relieved Face
    "\ud83d\ude2e", // Face With Open Mouth
    "\ud83e\udd10", // Zipper-Mouth Face
    "\ud83d\ude2f", // Hushed Face
    "\ud83d\ude2a", // Sleepy Face
    "\ud83d\ude2b", // Tired Face
    "\ud83d\ude34", // Sleeping Face
    "\ud83d\ude0c", // Relieved Face
    "\ud83d\ude1b", // Face With Stuck-Out Tongue
    "\ud83d\ude1c", // Face With Stuck-Out Tongue and Winking Eye
    "\ud83d\ude1d", // Face With Stuck-Out Tongue and Tightly-Closed Eyes
    "\ud83d\ude12", // Unamused Face
    "\ud83d\ude13", // Face With Cold Sweat
    "\ud83d\ude14", // Pensive Face
    "\ud83d\ude15", // Confused Face
    "\ud83d\ude43", // Upside-Down Face
    "\ud83e\udd11", // Money-Mouth Face
    "\ud83d\ude32", // Astonished Face
    "\ud83d\ude37", // Face With Medical Mask
    "\ud83e\udd12", // Face With Thermometer
    "\ud83e\udd15", // Face With Head-Bandage
    "\u2639", // White Frowning Face
    "\ud83d\ude41", // Slightly Frowning Face
    "\ud83d\ude16", // Confounded Face
    "\ud83d\ude1e", // Disappointed Face
    "\ud83d\ude1f", // Worried Face
    "\ud83d\ude24", // Face With Look of Triumph
    "\ud83d\ude22", // Crying Face
    "\ud83d\ude2d", // Loudly Crying Face
    "\ud83d\ude26", // Frowning Face With Open Mouth
    "\ud83d\ude27", // Anguished Face
    "\ud83d\ude28", // Fearful Face
    "\ud83d\ude29", // Weary Face
    "\ud83d\ude2c", // Grimacing Face
    "\ud83d\ude30", // Face With Open Mouth and Cold Sweat
    "\ud83d\ude31", // Face Screaming in Fear
    "\ud83d\ude33", // Flushed Face
    "\ud83d\ude35", // Dizzy Face
    "\ud83d\ude21", // Pouting Face
    "\ud83d\ude20", // Angry Face
    "\ud83d\ude08", // Smiling Face With Horns
    "\ud83d\udc7f", // Imp
    "\ud83d\udc79", // Japanese Ogre
    "\ud83d\udc7a", // Japanese Goblin
    "\ud83d\udc80", // Skull
    "\ud83d\udc7b", // Ghost
    "\ud83d\udc7d", // Extraterrestrial Alien
    "\ud83e\udd16", // Robot Face
    "\ud83d\udca9", // Pile of Poo
    "\ud83d\ude3a", // Smiling Cat Face With Open Mouth
    "\ud83d\ude38", // Grinning Cat Face With Smiling Eyes
    "\ud83d\ude39", // Cat Face With Tears of Joy
    "\ud83d\ude3b", // Smiling Cat Face With Heart-Shaped Eyes
    "\ud83d\ude3c", // Cat Face With Wry Smile
    "\ud83d\ude3d", // Kissing Cat Face With Closed Eyes
    "\ud83d\ude40", // Weary Cat Face
    "\ud83d\ude3f", // Crying Cat Face
    "\ud83d\ude3e", // Pouting Cat Face
    "\ud83d\udc66", // Boy
    "\ud83d\udc67", // Girl
    "\ud83d\udc68", // Man
    "\ud83d\udc69", // Woman
    "\ud83d\udc74", // Older Man
    "\ud83d\udc75", // Older Woman
    "\ud83d\udc76", // Baby
    "\ud83d\udc71", // Person With Blond Hair
    "\ud83d\udc6e", // Police Officer
    "\ud83d\udc72", // Man With Gua Pi Mao
    "\ud83d\udc73", // Man With Turban
    "\ud83d\udc77", // Construction Worker
    "\u26d1", // Helmet With White Cross
    "\ud83d\udc78", // Princess
    "\ud83d\udc82", // Guardsman
    "\ud83d\udd75", // Sleuth or Spy
    "\ud83c\udf85", // Father Christmas
    "\ud83d\udc70", // Bride With Veil
    "\ud83d\udc7c", // Baby Angel
    "\ud83d\udc86", // Face Massage
    "\ud83d\udc87", // Haircut
    "\ud83d\ude4d", // Person Frowning
    "\ud83d\ude4e", // Person With Pouting Face
    "\ud83d\ude45", // Face With No Good Gesture
    "\ud83d\ude46", // Face With OK Gesture
    "\ud83d\udc81", // Information Desk Person
    "\ud83d\ude4b", // Happy Person Raising One Hand
    "\ud83d\ude47", // Person Bowing Deeply
    "\ud83d\ude4c", // Person Raising Both Hands in Celebration
    "\ud83d\ude4f", // Person With Folded Hands
    "\ud83d\udde3", // Speaking Head in Silhouette
    "\ud83d\udc64", // Bust in Silhouette
    "\ud83d\udc65", // Busts in Silhouette
    "\ud83d\udeb6", // Pedestrian
    "\ud83c\udfc3", // Runner
    "\ud83d\udc6f", // Woman With Bunny Ears
    "\ud83d\udc83", // Dancer
    "\ud83d\udd74", // Man in Business Suit Levitating
    "\ud83d\udc6b", // Man and Woman Holding Hands
    "\ud83d\udc6c", // Two Men Holding Hands
    "\ud83d\udc6d", // Two Women Holding Hands
    "\ud83d\udc8f" // Kiss
)
