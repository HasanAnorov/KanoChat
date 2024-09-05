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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.RandomColors
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChatMessageEntity
import com.ierusalem.androchat.features_local.tcp.domain.model.ChattingUser

@Composable
fun TcpContactItem(
    modifier: Modifier = Modifier,
    contact: ChattingUser,
    lastMessage: ChatMessageEntity? = null,
    onClick: () -> Unit,
) {
    log("TcpContactItem: ${contact.isOnline}")
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
                    log("contact username ${contact.username}")
                    val username = try {
                        contact.username.first().toString()
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
                    text = contact.username,
                    color = MaterialTheme.colorScheme.onBackground
                )
                lastMessage?.let {
                    val lastMessageHint = when (it.type) {
                        AppMessageType.CONTACT -> "Contact Message"
                        AppMessageType.FILE -> "File Message"
                        AppMessageType.TEXT -> it.text ?: "Text Message"
                        AppMessageType.VOICE -> "Voice Message"
                        else -> "Unknown Message"
                    }
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.outline,
                        text = lastMessageHint,
                        fontSize = 14.sp
                    )
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

@Preview
@Composable
fun ContactItemPreview() {
    AndroChatTheme {
        TcpContactItem(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            onClick = {},
            contact = ChattingUser(
                userUniqueId = "249141sadfs67df9s7f89s7f",
                username = "Hasan",
                isOnline = false,
                avatarBackgroundColor = RandomColors().getColor(),
                lastMessage = null
            ),
            lastMessage = ChatMessageEntity(
                id = 324242,
                formattedTime = "12:10:23",
                isFromYou = true,
                text = "Hello",
                type = AppMessageType.TEXT,
                peerUniqueId = "sa79789s7f98s7s",
                authorUniqueId = "sfsdf"
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
                userUniqueId = "249141sadfs67df9s7f89s7f",
                username = "Hasan",
                isOnline = true,
                avatarBackgroundColor = RandomColors().getColor(),
                lastMessage = null
            ),
            lastMessage = ChatMessageEntity(
                id = 324242,
                formattedTime = "12:10:23",
                isFromYou = true,
                text = "Hello",
                type = AppMessageType.CONTACT,
                peerUniqueId = "sa79789s7f98s7s",
                authorUniqueId = "dsfadtww3r53"
            )
        )
    }
}