package com.ierusalem.androchat.features_tcp.tcp_chat.presentation.components

import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.ierusalem.androchat.features.auth.register.domain.model.FileState
import com.ierusalem.androchat.features.auth.register.domain.model.Message

@Composable
fun FileMessageItem(
    modifier: Modifier = Modifier,
    message: Message.FileMessage,
    onFileItemClick: (Message.FileMessage) -> Unit,
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
                    is FileState.Loading -> {
                        log("in file item progress - ${state.percentage}")
                        CircularProgressBar(percentage = (state.percentage.toFloat() / 100))
                    }

                    FileState.Success -> {
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

                    FileState.Failure -> {
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
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = message.formattedTime,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.8F),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
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
                message = Message.FileMessage(
                    formattedTime = "12:12:12, jul 12 2034",
                    username = "Hasan",
                    fileName = "SamsungElectronics Dubai Global Version home.edition.com",
                    fileSize = "16 Kb",
                    fileExtension = ".pdf",
                    filePath = Uri.EMPTY,
                    isFromYou = false
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
                message = Message.FileMessage(
                    formattedTime = "12:12:12, jul 12 2034",
                    username = "Hasan",
                    fileName = "SamsungElectronics Dubai Global Version home.edition.com",
                    fileSize = "16 Kb",
                    fileExtension = ".pdf",
                    filePath = Uri.EMPTY,
                    isFromYou = true
                ),
                onFileItemClick = {}
            )
        }
    }
}
