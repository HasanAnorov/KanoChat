package com.ierusalem.androchat.features_local.tcp.presentation.tcp_chats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.Constants.getRandomColor
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChatMessageEntity
import com.ierusalem.androchat.features_local.tcp.domain.model.ChattingUser

@Composable
fun TcpContactItem(
    modifier: Modifier = Modifier,
    contact: ChattingUser,
    lastMessage: ChatMessageEntity? = null,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .padding(end = 12.dp, start = 8.dp)
                .padding(top = 4.dp)
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(contact.avatarBackgroundColor)),
                    contentAlignment = Alignment.Center
                ) {
                    val username = try {
                        contact.partnerUsername.first().toString()
                    } catch (e: Exception) {
                        "?"
                    }
                    Text(
                        text = username,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                if (contact.isOnline) {
                    Box(
                        modifier = Modifier
                            .padding(end = 2.dp, bottom = 2.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.Green)
                            .align(Alignment.BottomEnd)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1F)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 16.sp,
                    text = contact.partnerUsername,
                    color = MaterialTheme.colorScheme.onBackground
                )

                lastMessage?.let { message ->

                    when (message.type) {
                        AppMessageType.CONTACT -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    modifier = Modifier.size(16.dp),
                                    painter = painterResource(id = R.drawable.call),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 4.dp),
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelSmall,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.outline,
                                    text = stringResource(R.string.contact_shared),
                                )
                            }
                        }

                        AppMessageType.FILE -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    modifier = Modifier.size(16.dp),
                                    painter = painterResource(id = R.drawable.file_text),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 4.dp),
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelSmall,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.outline,
                                    text = stringResource(R.string.file_shared),
                                )
                            }
                        }

                        AppMessageType.TEXT -> {
                            message.text?.let {
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelMedium,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.outline,
                                    text = it,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        AppMessageType.VOICE -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    modifier = Modifier.size(16.dp),
                                    painter = painterResource(id = R.drawable.voice_square),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 4.dp),
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelSmall,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.outline,
                                    text = stringResource(R.string.voice_message_sent),
                                )
                            }
                        }

                        else -> {

                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                lastMessage?.let {
                    Text(
                        text = it.formattedTime,
                        fontSize = 10.sp,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Preview(locale = "ru")
@Composable
fun ContactItemPreview() {
    AndroChatTheme {
        TcpContactItem(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            onClick = {},
            contact = ChattingUser(
                partnerSessionID = "249141sadfs67df9s7f89s7f",
                partnerUsername = "Hasan",
                isOnline = false,
                avatarBackgroundColor = getRandomColor(),
                lastMessage = null,
            ),
            lastMessage = ChatMessageEntity(
                id = 324242,
                formattedTime = "12:10:23",
                isFromYou = true,
                text = "Hello",
                type = AppMessageType.TEXT,
                partnerSessionId = "sa79789s7f98s7s",
                authorSessionId = "sfsdf",
            )
        )
    }
}

@Preview
@Composable
fun ContactItemPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        TcpContactItem(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            contact = ChattingUser(
                partnerSessionID = "249141sadfs67df9s7f89s7f",
                partnerUsername = "Hasan",
                isOnline = true,
                avatarBackgroundColor = getRandomColor(),
                lastMessage = null,
            ),
            lastMessage = ChatMessageEntity(
                id = 324242,
                formattedTime = "12:10:23",
                isFromYou = true,
                text = "Hello",
                type = AppMessageType.CONTACT,
                partnerSessionId = "sa79789s7f98s7s",
                authorSessionId = "dsfadtww3r53"
            )
        )
    }
}