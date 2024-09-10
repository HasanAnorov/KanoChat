package com.ierusalem.androchat.features_common.auth.register.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.components.CommonPasswordTextField
import com.ierusalem.androchat.core.ui.components.CommonTextFieldWithError
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.features_common.auth.register.domain.RegistrationScreenState

@Composable
fun RegistrationScreen(
    state: RegistrationScreenState,
    intentReducer: (RegistrationFormEvents) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .background(color = MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        content = {
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                text = stringResource(R.string.welcome),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displayLarge,
            )
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                text = stringResource(R.string.register_to_continue),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            CommonTextFieldWithError(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(horizontal = 16.dp),
                placeHolder = stringResource(R.string.username),
                value = state.username,
                errorMessage = state.usernameError,
                onTextChanged = {
                    intentReducer(RegistrationFormEvents.UsernameChanged(it))
                }
            )

            CommonPasswordTextField(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(horizontal = 16.dp),
                label = stringResource(R.string.password),
                value = state.password,
                errorMessage = state.passwordError,
                passwordVisibility = state.passwordVisibility,
                onPasswordVisibilityChanged = {
                    intentReducer(RegistrationFormEvents.PasswordVisibilityChanged)
                },
                onPasswordTextChanged = {
                    intentReducer(RegistrationFormEvents.PasswordChanged(it))
                }
            )

            CommonPasswordTextField(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(horizontal = 16.dp),
                label = stringResource(R.string.password_confirmation),
                value = state.repeatedPassword,
                passwordVisibility = state.passwordVisibility,
                errorMessage = state.repeatedPasswordError,
                onPasswordVisibilityChanged = {
                    intentReducer(RegistrationFormEvents.PasswordVisibilityChanged)
                },
                onPasswordTextChanged = {
                    intentReducer(RegistrationFormEvents.RepeatedPasswordChanged(it))
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color = MaterialTheme.colorScheme.primary)
                    .clickable { intentReducer(RegistrationFormEvents.Register) },
                content = {
                    Text(
                        text = stringResource(R.string.register),
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = {
                    Text(
                        text = stringResource(R.string.have_an_account),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .padding(vertical = 6.dp)
                            .clickable { intentReducer(RegistrationFormEvents.ToLogin) },
                        text = stringResource(R.string.log_in),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    )
}

@Preview
@Composable
fun LoginScreen_Preview_Light() {
    AndroChatTheme {
        RegistrationScreen(
            state = RegistrationScreenState(),
            intentReducer = {}
        )
    }
}

@Preview
@Composable
fun LoginScreen_Preview_Dark() {
    AndroChatTheme(isDarkTheme = true) {
        RegistrationScreen(
            state = RegistrationScreenState(),
            intentReducer = {}
        )
    }
}