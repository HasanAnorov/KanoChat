package com.ierusalem.androchat.features_tcp.tcp_chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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
import com.ierusalem.androchat.features_tcp.tcp_chat.presentation.ChatBubbleShapeEnd
import com.ierusalem.androchat.features_tcp.tcp_chat.presentation.ChatBubbleShapeStart

@Composable
fun FileMessageItem(
    modifier: Modifier = Modifier,
    message: ChatMessage.FileMessage,
    onFileItemClick: (ChatMessage.FileMessage) -> Unit,
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
                when (val state = message.fileState) {
                    is FileMessageState.Loading -> {
                        log("in file item progress - ${state.percentage}")
                        if(state.percentage == 100){
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .border(
                                        1.5.dp,
                                        MaterialTheme.colorScheme.tertiary,
                                        CircleShape
                                    )
                                    .clip(CircleShape)
                                    .clickable(
                                        onClick = {
                                            onFileItemClick(message)
                                        }
                                    )
                                    .background(MaterialTheme.colorScheme.background)
                                    .align(Alignment.Top),
                                contentAlignment = Alignment.Center,
                                content = {
                                    Icon(
                                        modifier = Modifier.size(28.dp),
                                        painter = painterResource(id = R.drawable.file_text),
                                        contentDescription = null
                                    )
                                }
                            )
                        }else{
                            CircularProgressBar(percentage = (state.percentage.toFloat() / 100))
                        }
                    }

                    FileMessageState.Success -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .border(
                                    1.5.dp,
                                    MaterialTheme.colorScheme.tertiary,
                                    CircleShape
                                )
                                .clip(CircleShape)
                                .clickable(
                                    onClick = {
                                        onFileItemClick(message)
                                    }
                                )
                                .background(MaterialTheme.colorScheme.background)
                                .align(Alignment.Top),
                            contentAlignment = Alignment.Center,
                            content = {
                                Icon(
                                    modifier = Modifier.size(28.dp),
                                    painter = painterResource(id = R.drawable.file_text),
                                    contentDescription = null
                                )
                            }
                        )
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
                                    modifier = Modifier.size(28.dp),
                                    painter = painterResource(id = R.drawable.file_failed),
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
                Column(
                    modifier = Modifier.padding(start = 8.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = message.fileName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = message.fileSize,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                        style = MaterialTheme.typography.titleSmall
                    )
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

@Preview
@Composable
private fun PreviewLightFileItemPeer() {
    AndroChatTheme {
        Surface {
            FileMessageItem(
                modifier = Modifier,
                message = ChatMessage.FileMessage(
                    formattedTime = "12:12:12, jul 12 2034",
                    fileName = "SamsungElectronics Dubai Global Version home.edition.com",
                    fileSize = "16 Kb",
                    fileExtension = ".pdf",
                    filePath = "file_path_uri",
                    isFromYou = false,
                    messageId = 0L,
                    peerUsername = "Khasan"
                ),
                onFileItemClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDarkFileItem() {
    AndroChatTheme(isDarkTheme = true) {
        Surface {
            FileMessageItem(
                modifier = Modifier,
                message = ChatMessage.FileMessage(
                    formattedTime = "12:12:12, jul 12 2034",
                    fileName = "SamsungElectronics Dubai Global Version home.edition.com",
                    fileSize = "16 Kb",
                    fileExtension = ".pdf",
                    filePath = "file_path_uri",
                    isFromYou = true,
                    messageId = 0L,
                    peerUsername = "Khasan"
                ),
                onFileItemClick = {}
            )
        }
    }
}


@Preview
@Composable
private fun PreviewLightFileItem() {
    AndroChatTheme {
        Surface {
            FileMessageItem(
                modifier = Modifier,
                message = ChatMessage.FileMessage(
                    formattedTime = "12:12:12, jul 12 2034",
                    fileName = "SamsungElectronics Dubai Global Version home.edition.com",
                    fileSize = "16 Kb",
                    fileExtension = ".pdf",
                    filePath = "file_path_uri",
                    isFromYou = true,
                    messageId = 0L,
                    peerUsername = "Khasan"
                ),
                onFileItemClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDarkFileItemPeer() {
    AndroChatTheme(isDarkTheme = true) {
        Surface {
            FileMessageItem(
                modifier = Modifier,
                message = ChatMessage.FileMessage(
                    formattedTime = "12:12:12, jul 12 2034",
                    fileName = "SamsungElectronics Dubai Global Version home.edition.com",
                    fileSize = "16 Kb",
                    fileExtension = ".pdf",
                    filePath = "file_path_uri",
                    isFromYou = false,
                    messageId = 0L,
                    peerUsername = "Khasan"
                ),
                onFileItemClick = {}
            )
        }
    }
}
