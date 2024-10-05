package com.ierusalem.androchat.features_local.tcp.presentation.tcp_networking

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.ierusalem.androchat.features_local.tcp.domain.state.GeneralNetworkingStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.HotspotNetworkingStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.LocalOnlyHotspotStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.P2PNetworkingStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenUiState
import com.ierusalem.androchat.features_local.tcp.presentation.TcpScreenEvents
import com.ierusalem.androchat.features_local.tcp.presentation.tcp_networking.components.LoadingAnimation
import com.ierusalem.androchat.features_local.tcp.presentation.tcp_networking.components.StatusProperty
import com.ierusalem.androchat.features_local.tcp.presentation.tcp_networking.components.WifiLazyItem

@Composable
fun NetworkingContent(
    modifier: Modifier = Modifier,
    eventHandler: (TcpScreenEvents) -> Unit,
    uiState: TcpScreenUiState,
) {
    val hotspotPasswordFocusRequester = remember { FocusRequester() }
    val hotspotNameFocusRequester = remember { FocusRequester() }
    LazyColumn(
        modifier = modifier.navigationBarsPadding()
    ) {
        item {
            Column(
                modifier = Modifier
                    .padding(top = 16.dp)
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
                                    .clickable { eventHandler(TcpScreenEvents.DiscoverLocalOnlyHotSpotClick) }
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
                                    text = stringResource(id = uiState.localOnlyHotspotNetworkingStatus.res),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Icon(
                                    modifier = Modifier.padding(start = 8.dp),
                                    painter = painterResource(id = uiState.localOnlyHotspotNetworkingStatus.icon),
                                    contentDescription = null,
                                    tint = uiState.localOnlyHotspotNetworkingStatus.getIconColor()
                                )
                            }
                            AnimatedVisibility(visible = uiState.localOnlyHotspotNetworkingStatus != LocalOnlyHotspotStatus.Idle) {
                                Column(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(
                                                0.3F
                                            )
                                        )
                                        .padding(horizontal = 10.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.local_only_hotspot_name),
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
                                            value = uiState.localOnlyHotspotName.ifEmpty {
                                                stringResource(
                                                    id = R.string.error_occurred
                                                )
                                            },
                                            textStyle = MaterialTheme.typography.titleMedium,
                                            colors = TextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                disabledTextColor = if (uiState.localOnlyHotspotPassword.isEmpty()) Color.Red else MaterialTheme.colorScheme.onSurface,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                disabledIndicatorColor = Color.Transparent
                                            ),
                                            onValueChange = {},
                                            placeholder = {},
                                            shape = RoundedCornerShape(size = 12.dp),
                                            singleLine = true,
                                            enabled = false
                                        )
                                    }
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.background
                                    )
                                    Column(
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.local_only_hotspot_password),
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
                                            value = uiState.localOnlyHotspotPassword.ifEmpty {
                                                stringResource(
                                                    id = R.string.error_occurred
                                                )
                                            },
                                            textStyle = MaterialTheme.typography.titleMedium,
                                            colors = TextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                disabledTextColor = if (uiState.localOnlyHotspotPassword.isEmpty()) Color.Red else MaterialTheme.colorScheme.onSurface,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                disabledIndicatorColor = Color.Transparent
                                            ),
                                            onValueChange = {},
                                            placeholder = {},
                                            shape = RoundedCornerShape(size = 12.dp),
                                            singleLine = true,
                                            enabled = false
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.background
                            )
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        if(uiState.canUseCustomConfigForHotspot){
                                            when{
                                                !uiState.isValidHotSpotPassword -> {
                                                    hotspotPasswordFocusRequester.requestFocus()
                                                }
                                                !uiState.isValidHotSpotName -> {
                                                    hotspotNameFocusRequester.requestFocus()
                                                }
                                                else -> {
                                                    eventHandler(TcpScreenEvents.DiscoverHotSpotClick)
                                                }
                                            }
                                        }else{
                                            eventHandler(TcpScreenEvents.DiscoverHotSpotClick)
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
                                    text = stringResource(id = uiState.hotspotNetworkingStatus.res),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Icon(
                                    modifier = Modifier.padding(start = 8.dp),
                                    painter = painterResource(id = uiState.hotspotNetworkingStatus.icon),
                                    contentDescription = null,
                                    tint = uiState.hotspotNetworkingStatus.getIconColor()
                                )
                            }
                            val staticHotspotNameAndPasswordVisibility by rememberSaveable(
                                uiState.canUseCustomConfigForHotspot,
                                uiState.hotspotNetworkingStatus
                            ) {
                                mutableStateOf(
                                    !uiState.canUseCustomConfigForHotspot &&
                                            uiState.hotspotNetworkingStatus != HotspotNetworkingStatus.Idle
                                )
                            }
                            AnimatedVisibility(visible = staticHotspotNameAndPasswordVisibility) {
                                Column(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(
                                                0.3F
                                            )
                                        )
                                        .padding(horizontal = 10.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.hotspot_name),
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
                                            value = uiState.staticHotspotName.ifEmpty {
                                                stringResource(
                                                    id = R.string.error_occurred
                                                )
                                            },
                                            textStyle = MaterialTheme.typography.titleMedium,
                                            colors = TextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                disabledTextColor = if (uiState.staticHotspotName.isEmpty()) Color.Red else MaterialTheme.colorScheme.onSurface,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                disabledIndicatorColor = Color.Transparent
                                            ),
                                            onValueChange = {},
                                            placeholder = {},
                                            shape = RoundedCornerShape(size = 12.dp),
                                            singleLine = true,
                                            enabled = false
                                        )
                                    }
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.background
                                    )
                                    Column(
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.hotspot_password),
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
                                            value = uiState.staticHotspotPassword.ifEmpty {
                                                stringResource(
                                                    id = R.string.error_occurred
                                                )
                                            },
                                            textStyle = MaterialTheme.typography.titleMedium,
                                            colors = TextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                disabledTextColor = if (uiState.staticHotspotPassword.isEmpty()) Color.Red else MaterialTheme.colorScheme.onSurface,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                disabledIndicatorColor = Color.Transparent
                                            ),
                                            onValueChange = {},
                                            placeholder = {},
                                            shape = RoundedCornerShape(size = 12.dp),
                                            singleLine = true,
                                            enabled = false
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.background
                            )
                            Row(
                                modifier = Modifier
                                    .clickable { eventHandler(TcpScreenEvents.DiscoverP2PClick) }
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
                                    text = stringResource(id = uiState.p2pNetworkingStatus.res),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Icon(
                                    modifier = Modifier.padding(start = 8.dp),
                                    painter = painterResource(id = uiState.p2pNetworkingStatus.icon),
                                    contentDescription = null,
                                    tint = uiState.p2pNetworkingStatus.getIconColor()
                                )
                            }
                        }
                    }
                )
                if(uiState.canUseCustomConfigForHotspot){
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        val hotspotName by remember(uiState.hotspotName) {
                            mutableStateOf(
                                TextFieldValue(
                                    uiState.hotspotName,
                                    TextRange(uiState.hotspotName.length)
                                )
                            )
                        }
                        Text(
                            text = stringResource(R.string.hotspot_name),
                            fontFamily = MontserratFontFamily,
                            modifier = Modifier.baselineHeight(20.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextField(
                            modifier = Modifier
                                .focusRequester(hotspotNameFocusRequester)
                                .height(IntrinsicSize.Max)
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            value = hotspotName,
                            textStyle = MaterialTheme.typography.titleMedium,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            onValueChange = {
                                eventHandler(TcpScreenEvents.OnHotspotNameChanged(it.text))
                            },
                            placeholder = {
                                Text(text = stringResource(R.string.enter_hotspot_name))
                            },
                            trailingIcon = {
                                if (uiState.isValidHotSpotName) {
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
                                imeAction = ImeAction.Next,
                                keyboardType = KeyboardType.Text
                            ),
                            shape = RoundedCornerShape(size = 12.dp),
                            singleLine = true,
                        )
                        if (!uiState.isValidHotSpotName) {
                            Text(
                                modifier = Modifier.padding(top = 4.dp),
                                text = stringResource(R.string.invalid_hotspot_name),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Red.copy(0.8F),
                                maxLines = 2,
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.background
                    )
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        val hotspotPassword by remember(uiState.hotspotPassword) {
                            mutableStateOf(
                                TextFieldValue(
                                    uiState.hotspotPassword,
                                    TextRange(uiState.hotspotPassword.length)
                                )
                            )
                        }
                        Text(
                            text = stringResource(R.string.hotspot_password),
                            fontFamily = MontserratFontFamily,
                            modifier = Modifier.baselineHeight(20.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextField(
                            modifier = Modifier
                                .focusRequester(hotspotPasswordFocusRequester)
                                .height(IntrinsicSize.Max)
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            value = hotspotPassword,
                            textStyle = MaterialTheme.typography.titleMedium,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            onValueChange = {
                                eventHandler(TcpScreenEvents.OnHotspotPasswordChanged(it.text))
                            },
                            placeholder = {
                                Text(text = stringResource(R.string.enter_hotspot_password))
                            },
                            trailingIcon = {
                                if (uiState.isValidHotSpotPassword) {
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
                                keyboardType = KeyboardType.Text
                            ),
                            shape = RoundedCornerShape(size = 12.dp),
                            singleLine = true,
                        )
                        if (!uiState.isValidHotSpotPassword) {
                            Text(
                                modifier = Modifier.padding(top = 4.dp),
                                text = stringResource(R.string.password_length_should_be_between_8_16_symbols),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Red.copy(0.8F),
                                maxLines = 2,
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.background
                    )
                }
                StatusProperty(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(top = 8.dp),
                    status = stringResource(R.string.wifi_status),
                    state = if (uiState.isWifiOn) R.string.wifi_enabled else R.string.wifi_disabled,
                    stateColor = if (uiState.isWifiOn) Color(0xFF35C47C) else Color.Red
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.background
                )
                StatusProperty(
                    modifier = Modifier
                        .padding(horizontal = 10.dp),
                    status = stringResource(R.string.networking_status),
                    state = uiState.generalNetworkingStatus.status,
                    stateColor = if (uiState.generalNetworkingStatus != GeneralNetworkingStatus.Idle) Color(
                        0xFF35C47C
                    ) else Color.Red
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
                if (uiState.availableWifiNetworks.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(54.dp, 200.dp)
                            .padding(horizontal = 10.dp)
                            .padding(top = 16.dp)
                    ) {
                        items(uiState.availableWifiNetworks) { wifiDevice ->
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
                when (uiState.p2pNetworkingStatus) {
                    P2PNetworkingStatus.Idle -> {
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

                    P2PNetworkingStatus.Discovering -> {
                        LoadingAnimation(
                            modifier = Modifier.padding(vertical = 16.dp),
                            circleSize = 10.dp,
                            travelDistance = 8.dp,
                            spaceBetween = 6.dp
                        )
                    }

                    P2PNetworkingStatus.Failure -> {
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
        NetworkingContent(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            eventHandler = {},
            uiState = TcpScreenUiState(
                isValidHotSpotPassword = false
            )
        )
    }
}

@Preview(locale = "ru")
@Composable
private fun HotspotContentPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        NetworkingContent(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            eventHandler = {},
            uiState = TcpScreenUiState(
                isValidHotSpotPassword = false
            )
        )
    }
}