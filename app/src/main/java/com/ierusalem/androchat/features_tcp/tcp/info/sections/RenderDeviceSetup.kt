/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ierusalem.androchat.features_tcp.tcp.info.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ierusalem.androchat.R
import com.ierusalem.androchat.features_tcp.tcp.info.domain.InfoScreenUiState
import com.ierusalem.androchat.features_tcp.tcp.tcp.domain.TcpScreenUiState
import com.ierusalem.androchat.ui.tcp.ServerViewState
import com.ierusalem.androchat.ui.tcp.TestServerViewState
import com.ierusalem.androchat.ui.tcp.rememberServerHostname
import com.ierusalem.androchat.ui.tcp.rememberServerPassword
import com.ierusalem.androchat.ui.tcp.rememberServerRawPassword
import com.ierusalem.androchat.ui.tcp.rememberServerSSID

private enum class DeviceSetupContentTypes {
  SETTINGS,
  CONNECT,
  TOGGLE,
}

internal fun LazyListScope.renderDeviceSetup(
    itemModifier: Modifier = Modifier,
    appName: String,
    state: TcpScreenUiState,
    serverViewState: ServerViewState,
    onShowQRCode: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
) {
  item(
      contentType = DeviceSetupContentTypes.SETTINGS,
  ) {
    OtherInstruction(
        modifier = itemModifier,
    ) {
      Text(
          text = stringResource(R.string.open_wifi_settings),
          style = MaterialTheme.typography.bodyLarge,
      )
    }
  }

  item(
      contentType = DeviceSetupContentTypes.CONNECT,
  ) {
    OtherInstruction(
        modifier = itemModifier.padding(top = 16.dp),
    ) {
      Column {
        Text(
            text = stringResource(R.string.connect_to_hotspot, appName),
            style =
                MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
        )

        Row {
          val group by serverViewState.group.collectAsStateWithLifecycle()
          val ssid = rememberServerSSID(group)

          val password = rememberServerRawPassword(group)
          val isNetworkReadyForQRCode =
              remember(
                  ssid,
                  password,
              ) {
                ssid.isNotBlank() && password.isNotBlank()
              }

          Text(
              text = stringResource(R.string.label_hotspot_name),
              style =
                  MaterialTheme.typography.bodyLarge.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                  ),
          )

          Text(
              modifier = Modifier.padding(start = 16.dp),
              text = ssid,
              style =
                  MaterialTheme.typography.bodyLarge.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontWeight = FontWeight.W700,
                      fontFamily = FontFamily.Monospace,
                  ),
          )

          if (isNetworkReadyForQRCode) {
            // Don't use IconButton because we don't care about minimum touch target size
            Box(
                modifier =
                    Modifier.padding(start = 16.dp)
                        .clickable { onShowQRCode() }
                        .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
              Icon(
                  modifier = Modifier.size(16.dp),
                  imageVector = Icons.Filled.QrCode,
                  contentDescription = stringResource(R.string.qr_code),
                  tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }

        Row {
          val group by serverViewState.group.collectAsStateWithLifecycle()
          val isPasswordVisible = state.isPasswordVisible
          val password = rememberServerPassword(group, isPasswordVisible)
          val rawPassword = rememberServerRawPassword(group)

          Text(
              text = stringResource(R.string.password),
              style =
                  MaterialTheme.typography.bodyLarge.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                  ),
          )
          Text(
              modifier = Modifier.padding(start = 16.dp),
              text = password,
              style =
                  MaterialTheme.typography.bodyLarge.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontWeight = FontWeight.W700,
                      fontFamily = FontFamily.Monospace,
                  ),
          )

          if (rawPassword.isNotBlank()) {
            // Don't use IconButton because we don't care about minimum touch target size
            Box(
                modifier =
                    Modifier.padding(start = 16.dp)
                        .clickable {
                          onTogglePasswordVisibility()
                        }
                        .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
              Icon(
                  modifier = Modifier.size(16.dp),
                  imageVector =
                      if (isPasswordVisible) Icons.Filled.VisibilityOff
                      else Icons.Filled.Visibility,
                  contentDescription =
                      stringResource(
                          if (isPasswordVisible) R.string.pass_visible else R.string.pass_hidden),
                  tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(R.string.configure_the_proxy_settings),
            style = MaterialTheme.typography.bodyLarge,
        )

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(R.string.http_manual_proxy),
            style =
                MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
        )

        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          val connection by serverViewState.connection.collectAsStateWithLifecycle()
          val ipAddress = rememberServerHostname(connection)

          Text(
              text = stringResource(R.string.label_hotspot_hostname),
              style =
                  MaterialTheme.typography.bodyLarge.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                  ),
          )
          Text(
              modifier = Modifier.padding(start = 16.dp),
              text = ipAddress,
              style =
                  MaterialTheme.typography.bodyLarge.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontWeight = FontWeight.W700,
                      fontFamily = FontFamily.Monospace,
                  ),
          )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
              text = stringResource(R.string.label_hotspot_port),
              style =
                  MaterialTheme.typography.bodyLarge.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                  ),
          )

          val context = LocalContext.current
          val port by serverViewState.port.collectAsStateWithLifecycle()
          val portNumber =
              remember(
                  context,
                  port,
              ) {
                if (port <= 1024) context.getString(R.string.invalid_port) else "$port"
              }
          Text(
              modifier = Modifier.padding(start = 16.dp),
              text = portNumber,
              style =
                  MaterialTheme.typography.bodyLarge.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontWeight = FontWeight.W700,
                      fontFamily = FontFamily.Monospace,
                  ),
          )
        }
      }
    }
  }

  item(
      contentType = DeviceSetupContentTypes.TOGGLE,
  ) {
    OtherInstruction(
        modifier = itemModifier.padding(top = 16.dp),
    ) {
      Column {
        Text(
            text = stringResource(R.string.toggle_wifi),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = stringResource(R.string.automatically_connect, appName),
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
        )
      }
    }
  }
}

@Preview
@Composable
private fun PreviewDeviceSetup() {
  LazyColumn {
    renderDeviceSetup(
        appName = "TEST",
        state = TcpScreenUiState(),
        onTogglePasswordVisibility = {},
        onShowQRCode = {},
        serverViewState = TestServerViewState()
    )
  }
}
