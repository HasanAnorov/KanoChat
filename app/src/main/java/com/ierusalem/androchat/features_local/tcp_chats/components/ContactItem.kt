package com.ierusalem.androchat.features_local.tcp_chats.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChatMessageEntity
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChattingUserEntity

@Composable
fun TcpContactItem(
    modifier: Modifier = Modifier,
    contact: ChattingUserEntity,
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
            Image(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(50.dp)
                    .clip(CircleShape),
                painter = painterResource(id = R.drawable.setup),
                contentDescription = null,
            )
            Column(
                modifier = Modifier
                    .weight(1F)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 16.sp,
                    text = contact.userUniqueName,
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
            contact = ChattingUserEntity(
                userUniqueId = "249141sadfs67df9s7f89s7f",
                userUniqueName = "Hasan"
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
            contact = ChattingUserEntity(
                userUniqueId = "249141sadfs67df9s7f89s7f",
                userUniqueName = "Hasan"
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