package com.ierusalem.androchat.features_local.tcp_conversation.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.components.CircularProgressBar
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.core.utils.millisecondsToTime
import com.ierusalem.androchat.features_local.tcp.domain.model.AudioState
import com.ierusalem.androchat.features_local.tcp.domain.model.ChatMessage
import com.ierusalem.androchat.features_local.tcp.domain.state.FileMessageState

@Composable
fun VoiceMessageItem(
    modifier: Modifier = Modifier,
    message: ChatMessage.VoiceMessage,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit,
) {
    val backgroundBubbleColor = if (message.isFromYou) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.inverseOnSurface
    }
    Surface(
        color = backgroundBubbleColor,
        shape = if (message.isFromYou) ChatBubbleShapeEnd else ChatBubbleShapeStart,
    ) {
        Column(modifier.padding(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (message.fileState) {
                    is FileMessageState.Loading -> {
                        CircularProgressBar(percentage = (message.fileState.percentage.toFloat() / 100))
                        Column(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .weight(1F),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text(
                                    modifier = Modifier.padding(top = 4.dp),
                                    text = message.duration.millisecondsToTime(),
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Text(
                                text = message.formattedTime,
                                color = MaterialTheme.colorScheme.outline.copy(0.8F),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    FileMessageState.Success -> {
                        IconButton(
                            onClick = {
                                when (message.audioState) {
                                    is AudioState.Playing -> {
                                        onPauseClick()
                                    }

                                    else -> {
                                        onPlayClick()
                                    }
                                }
                            }
                        ) {
                            when (message.audioState) {
                                is AudioState.Playing -> {
                                    Icon(
                                        modifier = Modifier.size(42.dp),
                                        painter = painterResource(id = R.drawable.pause_circle_fill),
                                        contentDescription = null
                                    )
                                }

                                else -> {
                                    Icon(
                                        modifier = Modifier.size(42.dp),
                                        painter = painterResource(id = R.drawable.play_circle_fill),
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .weight(1F),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center
                        ) {
                            when (message.audioState) {
                                is AudioState.Paused -> {
                                    val progress =
                                        (message.audioState.currentPosition.toFloat() / message.duration)
                                    log("current position - ${message.audioState.currentPosition}, duration - ${message.duration}, progress - $progress")
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        progress = { progress },
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(0.1F),
                                    )
                                }

                                is AudioState.Playing -> {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        progress = { message.audioState.timing.toFloat() / 100 },
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(0.1F),
                                    )
                                }

                                AudioState.Idle -> {}
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                when (message.audioState) {
                                    is AudioState.Paused -> {
                                        Text(
                                            modifier = Modifier.padding(top = 4.dp),
                                            text = (message.audioState.currentPosition.toLong()).millisecondsToTime(),
                                            color = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            modifier = Modifier.padding(top = 4.dp),
                                            text = " / ",
                                            color = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }

                                    is AudioState.Playing -> {
                                        Text(
                                            modifier = Modifier.padding(top = 4.dp),
                                            text = ((message.duration * message.audioState.timing) / 100).millisecondsToTime(),
                                            color = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            modifier = Modifier.padding(top = 4.dp),
                                            text = " / ",
                                            color = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }

                                    is AudioState.Idle -> {}
                                }
                                Text(
                                    modifier = Modifier.padding(top = 4.dp),
                                    text = message.duration.millisecondsToTime(),
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Text(
                                text = message.formattedTime,
                                color = MaterialTheme.colorScheme.outline.copy(0.8F),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        when (message.audioState) {
                            is AudioState.Paused, is AudioState.Playing -> {
                                IconButton(onClick = { onStopClick() }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.close_circle),
                                        contentDescription = null
                                    )
                                }
                            }

                            AudioState.Idle -> {}
                        }
                    }
                    FileMessageState.Failure -> {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .border(
                                    1.5.dp,
                                    MaterialTheme.colorScheme.tertiary,
                                    CircleShape
                                )
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.background)
                                .align(Alignment.Top),
                            contentAlignment = Alignment.Center,
                            content = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null
                                )
                            }
                        )
                        Column(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .weight(1F),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text(
                                    modifier = Modifier.padding(top = 4.dp),
                                    text = message.duration.millisecondsToTime(),
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Text(
                                text = message.formattedTime,
                                color = MaterialTheme.colorScheme.outline.copy(0.8F),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewLightVoiceMessageItemPeer() {
    AndroChatTheme {
        Surface {
            VoiceMessageItem(
                modifier = Modifier,
                message = ChatMessage.VoiceMessage(
                    formattedTime = "12:12:12, jul 12 2034",
                    voiceFileName = "SamsungElectronics Dubai Global Version home.edition.com",
                    isFromYou = false,
                    duration = 12000,
                    audioState = AudioState.Paused(6000),
                    fileState = FileMessageState.Failure,
                    messageId = 0L,
                    peerUsername = "Khasan"
                ),
                onPlayClick = {},
                onPauseClick = {},
                onStopClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDarkVoiceMessageItemPeer() {
    AndroChatTheme(isDarkTheme = true) {
        Surface {
            VoiceMessageItem(
                modifier = Modifier,
                message = ChatMessage.VoiceMessage(
                    formattedTime = "12:12:12, jul 12 2034",
                    voiceFileName = "SamsungElectronics Dubai Global Version home.edition.com",
                    isFromYou = false,
                    duration = 80,
                    fileState = FileMessageState.Success,
                    messageId = 0L,
                    peerUsername = "Khasan"
                ),
                onPlayClick = {},
                onPauseClick = {},
                onStopClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewLightVoiceMessageItem() {
    AndroChatTheme {
        Surface {
            VoiceMessageItem(
                modifier = Modifier,
                message = ChatMessage.VoiceMessage(
                    formattedTime = "12:12:12, jul 12 2034",
                    voiceFileName = "SamsungElectronics Dubai Global Version home.edition.com",
                    isFromYou = true,
                    duration = 12000,
                    audioState = AudioState.Paused(6000),
                    fileState = FileMessageState.Failure,
                    messageId = 0L,
                    peerUsername = "Khasan"
                ),
                onPlayClick = {},
                onPauseClick = {},
                onStopClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDarkVoiceMessageItem() {
    AndroChatTheme(isDarkTheme = true) {
        Surface {
            VoiceMessageItem(
                modifier = Modifier,
                message = ChatMessage.VoiceMessage(
                    formattedTime = "12:12:12, jul 12 2034",
                    voiceFileName = "SamsungElectronics Dubai Global Version home.edition.com",
                    isFromYou = true,
                    duration = 80,
                    fileState = FileMessageState.Success,
                    messageId = 0L,
                    peerUsername = "Khasan"
                ),
                onPlayClick = {},
                onPauseClick = {},
                onStopClick = {},
            )
        }
    }
}