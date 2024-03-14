package com.ierusalem.androchat.features.auth.register.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.features.auth.login.LoginScreenState
import com.ierusalem.androchat.ui.theme.AndroChatTheme

@Composable
fun RegistrationScreen(
    state: LoginScreenState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        content = {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = "Welcome \nBack",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displayLarge,
            )
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                text = "Sign in to continue",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            TextField(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = "",
                textStyle = MaterialTheme.typography.titleMedium,
                colors = TextFieldDefaults.colors(
                    cursorColor = MaterialTheme.colorScheme.onPrimary,
                    disabledLabelColor = Color.Red,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                placeholder = {
                    Text(
                        text = "Username",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onValueChange = {},
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next, // ** Done. Close the keyboard **
                    keyboardType = KeyboardType.Text
                ),
                shape = RoundedCornerShape(size = 12.dp),
                singleLine = true,
            )

            TextField(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = "",
                textStyle = MaterialTheme.typography.titleMedium,
                colors = TextFieldDefaults.colors(
                    cursorColor = MaterialTheme.colorScheme.onPrimary,
                    disabledLabelColor = Color.Red,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                placeholder = {
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onValueChange = {},
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next, // ** Done. Close the keyboard **
                    keyboardType = KeyboardType.Text
                ),
                shape = RoundedCornerShape(size = 12.dp),
                singleLine = true,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color = MaterialTheme.colorScheme.primary)
                    .clickable { },
                content = {
                    Text(
                        text = "text",
                        modifier = Modifier
                            .padding(
                                horizontal = 16.dp,
                                vertical = 16.dp
                            )
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                },
            )
        }
    )
}

@Preview
@Composable
fun LoginScreen_Preview_Light() {
    AndroChatTheme {
        RegistrationScreen(
            state = LoginScreenState(),
            onUsernameChanged = {},
            onPasswordChanged = {},
            onLoginClick = {}
        )
    }
}

@Preview
@Composable
fun LoginScreen_Preview_Dark() {
    AndroChatTheme(isDarkTheme = true) {
        RegistrationScreen(
            state = LoginScreenState(),
            onUsernameChanged = {},
            onPasswordChanged = {},
            onLoginClick = {}
        )
    }
}