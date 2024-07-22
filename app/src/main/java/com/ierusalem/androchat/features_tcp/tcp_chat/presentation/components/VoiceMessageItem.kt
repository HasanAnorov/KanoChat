package com.ierusalem.androchat.features_tcp.tcp_chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.ChatMessage
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.FileMessageState

@Composable
fun VoiceMessageItem(
    modifier: Modifier = Modifier,
    message: ChatMessage.VoiceMessage,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    isPlaying: Boolean = false
) {
    val backgroundBubbleColor = if (message.isFromYou) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    Surface(
        color = backgroundBubbleColor,
        shape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    ) {
        Column(modifier.padding(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (val state = message.fileState) {
                    is FileMessageState.Loading -> {
                        log("in file item progress - ${state.percentage}")
                        CircularProgressBar(percentage = (state.percentage.toFloat() / 100))
                    }

                    FileMessageState.Success -> {
                        IconButton(
                            onClick = {
                                onPlayClick()
                                //if (isPlaying) onPauseClick() else onPlayClick()
                            }
                        ) {
                            val icon =
                                if (isPlaying) R.drawable.pause_circle_fill else R.drawable.play_circle_fill
                            Icon(
                                modifier = Modifier.size(32.dp),
                                painter = painterResource(id = icon),
                                contentDescription = null
                            )
                        }
                    }

                    FileMessageState.Failure -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
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
                                    modifier = Modifier.size(32.dp),
                                    painter = painterResource(id = R.drawable.file_failed),
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1F),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    LinearProgressIndicator(
                        progress = { 0.7F },
                        trackColor = MaterialTheme.colorScheme.tertiary,
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = (message.duration/1000).toString(),
                        color = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = message.formattedTime,
                        color = MaterialTheme.colorScheme.outline.copy(0.8F),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (isPlaying) {
                    IconButton(onClick = { onStopClick() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.close_circle),
                            contentDescription = null
                        )
                    }
                }
            }
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
                    username = "Hasan",
                    fileName = "SamsungElectronics Dubai Global Version home.edition.com",
                    fileSize = "16 Kb",
                    fileExtension = ".pdf",
                    filePath = "file_path_uri",
                    isFromYou = false,
                    duration = 12000,
                    fileState = FileMessageState.Success
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
                    username = "Hasan",
                    fileName = "SamsungElectronics Dubai Global Version home.edition.com",
                    fileSize = "16 Kb",
                    fileExtension = ".pdf",
                    filePath = "file_path_uri",
                    isFromYou = false,
                    duration = 12000,
                    fileState = FileMessageState.Success
                ),
                onPlayClick = {},
                onPauseClick = {},
                onStopClick = {},
                isPlaying = true,
            )
        }
    }
}