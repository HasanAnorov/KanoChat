package com.ierusalem.androchat.features_tcp.tcp_host

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.features_tcp.tcp.TcpScreenEvents
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpScreenUiState
import com.ierusalem.androchat.features_tcp.tcp.domain.WifiDiscoveryStatus
import com.ierusalem.androchat.features_tcp.tcp_host.components.HotspotButton
import com.ierusalem.androchat.features_tcp.tcp_host.components.LoadingAnimation
import com.ierusalem.androchat.features_tcp.tcp_host.components.StatusProperty
import com.ierusalem.androchat.features_tcp.tcp_host.components.WifiLazyItem
import com.ierusalem.androchat.ui.components.baselineHeight
import com.ierusalem.androchat.ui.theme.AndroChatTheme
import com.ierusalem.androchat.ui.theme.MontserratFontFamily

@Composable
fun HotSpotContent(
    modifier: Modifier = Modifier,
    eventHandler: (TcpScreenEvents) -> Unit,
    state: TcpScreenUiState,
) {
    LazyColumn(
        modifier = modifier.navigationBarsPadding()
    ) {
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
                                    .clickable { eventHandler(TcpScreenEvents.CreateServerClick) }
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
                                    text = stringResource(id = state.serverTitleStatus.status),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Icon(
                                    modifier = Modifier.padding(start = 8.dp),
                                    painter = painterResource(id = R.drawable.wifi_tethering),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
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
                                    text = stringResource(id = state.clientTitleStatus.status),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Icon(
                                    modifier = Modifier.padding(start = 8.dp),
                                    painter = painterResource(id = R.drawable.wifi_tethering),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
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
                    Text(
                        text = stringResource(R.string.proxy_port),
                        fontFamily = MontserratFontFamily,
                        modifier = Modifier.baselineHeight(20.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextField(
                        modifier = Modifier
                            .height(IntrinsicSize.Max)
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        value = state.portNumber,
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
                            if (it.length < 6) {
                                eventHandler(TcpScreenEvents.OnPortNumberChanged(it))
                            }
                        },
                        trailingIcon = {
                            if (state.isValidPortNumber) {
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
                    .padding(top = 16.dp)
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceDim.copy(0.2F)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HotspotButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(1.dp),
                    onClick = { eventHandler(TcpScreenEvents.DiscoverWifiClick) },
                    icon = painterResource(id = state.wifiDiscoveryStatus.icon),
                    title = state.wifiDiscoveryStatus.res
                )
                StatusProperty(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(top = 8.dp),
                    status = stringResource(R.string.wifi_status),
                    state = if (state.isWifiOn) R.string.wifi_enabled else R.string.wifi_disabled,
                    stateColor = if (state.isWifiOn) Color(0xFF35C47C) else Color.Red
                )
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
                        text = stringResource(R.string.available_wifi_networks),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.background,
                    thickness = 1.dp
                )
                if (state.availableWifiNetworks.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(54.dp, 200.dp)
                            .padding(horizontal = 10.dp)
                            .padding(top = 16.dp)
                    ) {
                        items(state.availableWifiNetworks) { wifiDevice ->
                            WifiLazyItem(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                onClick = {
                                    eventHandler(
                                        TcpScreenEvents.OnConnectToWifiClick(
                                            wifiDevice
                                        )
                                    )
                                },
                                wifiName = wifiDevice.deviceName
                            )
                        }
                    }
                }
                when (state.wifiDiscoveryStatus) {
                    WifiDiscoveryStatus.Idle -> {
                        Text(
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .padding(horizontal = 10.dp),
                            text = stringResource(R.string.click_discover_button_to_search_for_available_wifi_networks),
                            fontFamily = MontserratFontFamily,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    WifiDiscoveryStatus.Discovering -> {
                        LoadingAnimation(
                            modifier = Modifier.padding(vertical = 16.dp),
                            circleSize = 10.dp,
                            travelDistance = 8.dp,
                            spaceBetween = 6.dp
                        )
                    }

                    WifiDiscoveryStatus.Failure -> {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .padding(vertical = 16.dp),
                            text = stringResource(R.string.something_went_wrong_while_discovering_wifi_networks),
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
}

@Preview
@Composable
private fun HotspotContentPreview() {
    AndroChatTheme {
        HotSpotContent(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            eventHandler = {},
            state = TcpScreenUiState()
        )
    }
}

@Preview(locale = "ru")
@Composable
private fun HotspotContentPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        HotSpotContent(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            eventHandler = {},
            state = TcpScreenUiState()
        )
    }
}