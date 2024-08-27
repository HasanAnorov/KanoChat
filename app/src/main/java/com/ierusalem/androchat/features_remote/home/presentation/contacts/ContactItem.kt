package com.ierusalem.androchat.features_remote.home.presentation.contacts

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.ierusalem.androchat.core.constants.Constants
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme

@Composable
fun ContactItem(
    modifier: Modifier = Modifier,
    contact: ContactsScreenData,
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
                    text = contact.contactName,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    maxLines = 1,
                    style = MaterialTheme.typography.labelMedium,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.outline,
                    text = contact.lastMessage,
                    fontSize = 14.sp
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = contact.messageTime,
                    fontSize = 10.sp,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    val count = if(contact.unreadMessageCount> Constants.MAX_BADGE_COUNT) "99+" else contact.unreadMessageCount.toString()
                    Text(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        text = count,
                        fontSize = 10.sp,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.background
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
        ContactItem(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            onClick = {},
            contact = ContactsScreenData(
                "Hasan",
                "hey, I am preparing a big surprise for you :)",
                "23:21",
                12
            )
        )
    }
}

@Preview
@Composable
fun ContactItemPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        ContactItem(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            contact = ContactsScreenData(
                "Hasan",
                "hey, I am preparing a big surprise for you :)",
                "3:34",
                2
            )
        )
    }
}