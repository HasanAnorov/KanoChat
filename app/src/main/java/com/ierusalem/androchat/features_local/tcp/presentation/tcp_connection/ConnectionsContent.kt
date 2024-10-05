package com.ierusalem.androchat.features_local.tcp.presentation.tcp_connection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.components.baselineHeight
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.ui.theme.MontserratFontFamily
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenUiState
import com.ierusalem.androchat.features_local.tcp.presentation.TcpScreenEvents
import com.ierusalem.androchat.features_local.tcp.presentation.tcp_networking.components.StatusProperty
import com.ierusalem.androchat.features_local.tcp.presentation.tcp_networking.components.WifiLazyItem

@Composable
fun ConnectionsContent(
    modifier: Modifier = Modifier,
    eventHandler: (TcpScreenEvents) -> Unit,
    uiState: TcpScreenUiState
) {
    val portNumberFocusRequester = remember { FocusRequester() }
    LazyColumn(modifier = modifier) {
        item {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceDim.copy(0.2F)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = modifier,
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    content = {
                        Column {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        if(uiState.isValidPortNumber){
                                            eventHandler(TcpScreenEvents.CreateServerClick)
                                        }else{
                                            portNumberFocusRequester.requestFocus()
                                        }
                                    }
                                    .fillMaxWidth()
                                    .padding(
                                        vertical = 10.dp,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    text = stringResource(id = uiState.hostConnectionStatus.status),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Icon(
                                    modifier = Modifier.padding(start = 8.dp),
                                    painter = painterResource(id = R.drawable.wifi_tethering),
                                    contentDescription = null,
                                    tint = uiState.hostConnectionStatus.getIconColor()
                                )
                            }
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.background
                            )
                            Row(
                                modifier = Modifier
                                    .clickable { eventHandler(TcpScreenEvents.ConnectToServerClick) }
                                    .fillMaxWidth()
                                    .padding(
                                        vertical = 10.dp,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    text = stringResource(id = uiState.clientConnectionStatus.status),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Icon(
                                    modifier = Modifier.padding(start = 8.dp),
                                    painter = painterResource(id = R.drawable.wifi_tethering),
                                    contentDescription = null,
                                    tint = uiState.clientConnectionStatus.getIconColor()
                                )
                            }
                        }
                    }
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 16.dp)
                ) {
                    val portNumber by remember(uiState.portNumber) {
                        mutableStateOf(
                            TextFieldValue(
                                uiState.portNumber,
                                TextRange(uiState.portNumber.length)
                            )
                        )
                    }
                    Text(
                        text = stringResource(R.string.proxy_port),
                        fontFamily = MontserratFontFamily,
                        modifier = Modifier.baselineHeight(20.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextField(
                        modifier = Modifier
                            .focusRequester(portNumberFocusRequester)
                            .height(IntrinsicSize.Max)
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        value = portNumber,
                        textStyle = MaterialTheme.typography.titleMedium,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        placeholder = {
                            Text(text = stringResource(R.string._1024_65000))
                        },

                        onValueChange = {
                            if (it.text.length < 6) {
                                eventHandler(TcpScreenEvents.OnPortNumberChanged(it.text))
                            }
                        },
                        trailingIcon = {
                            if (uiState.isValidPortNumber) {
                                Icon(
                                    painter = painterResource(id = R.drawable.check_circle),
                                    contentDescription = null,
                                    tint = Color(0xFF35C47C)
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.error_sign),
                                    contentDescription = null,
                                    tint = Color.Red
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        ),
                        shape = RoundedCornerShape(size = 12.dp),
                        singleLine = true,
                    )
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceDim.copy(0.2F)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceDim.copy(0.4F)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        text = stringResource(R.string.connections_details),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.background,
                    thickness = 1.dp
                )
                StatusProperty(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    status = stringResource(R.string.connection_status),
                    state = uiState.generalConnectionStatus.status
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.server_address),
                        fontFamily = MontserratFontFamily,
                        modifier = Modifier.baselineHeight(20.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = uiState.connectedServerAddress.asString(),
                        modifier = Modifier.baselineHeight(24.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 16.dp)
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceDim.copy(0.2F)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceDim.copy(0.4F)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        text = stringResource(R.string.connected_devices_will_display_here),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.background,
                    thickness = 1.dp
                )
                if (uiState.connectedWifiNetworks.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .padding(top = 16.dp)
                            .height(200.dp)
                    ) {
                        items(uiState.connectedWifiNetworks) { wifiDevice ->
                            WifiLazyItem(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                onClick = {
                                    eventHandler(TcpScreenEvents.OnConnectToWifiClick(wifiDevice))
                                },
                                wifiName = wifiDevice.deviceName
                            )
                        }
                    }
                }else{
                    Text(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .padding(horizontal = 10.dp),
                        text = stringResource(R.string.connections_are_not_established_yet),
                        fontFamily = MontserratFontFamily,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            }
        }
    }
}

@Preview(locale = "ru")
@Composable
private fun ClientContentPreview() {
    AndroChatTheme {
        ConnectionsContent(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            eventHandler = {},
            uiState = TcpScreenUiState()
        )
    }
}

@Preview
@Composable
private fun ClientContentPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        ConnectionsContent(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            eventHandler = {},
            uiState = TcpScreenUiState()
        )
    }
}