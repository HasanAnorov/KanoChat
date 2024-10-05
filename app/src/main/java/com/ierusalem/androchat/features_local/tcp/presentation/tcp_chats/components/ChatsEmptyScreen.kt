package com.ierusalem.androchat.features_local.tcp.presentation.tcp_chats.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.ierusalem.androchat.R

@Composable
fun NoMessagesScreen(
    onCreateNetworkClick: () -> Unit = {  }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the icon
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.messages), // Replace with your vector resource
            contentDescription = "No Messages Icon",
            modifier = Modifier.size(150.dp) // Adjust size as needed
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title text
        Text(
            color = MaterialTheme.colorScheme.onBackground,
            text = stringResource(R.string.no_messages_yet),
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle text
        Text(
            text = stringResource(R.string.no_messages_in_your_inbox_yet_start_chatting_with_people_around_you),
            style = TextStyle(
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Create New button
        Button(
            shape = RoundedCornerShape(12.dp),
            onClick = { onCreateNetworkClick() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), // Use the color from the image
            modifier = Modifier.fillMaxWidth(0.6f) // Adjust the width of the button
        ) {
            Text(
                text = stringResource(R.string.create_network),
                color = Color.White,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNoMessagesScreen() {
    NoMessagesScreen()
}
