package com.ierusalem.androchat.features_tcp.tcp.info.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.features_tcp.tcp.info.domain.InfoScreenUiState
import com.ierusalem.androchat.features_tcp.tcp.info.sections.renderAppSetup
import com.ierusalem.androchat.features_tcp.tcp.info.sections.renderConnectionComplete
import com.ierusalem.androchat.features_tcp.tcp.info.sections.renderDeviceIdentifiers
import com.ierusalem.androchat.features_tcp.tcp.info.sections.renderDeviceSetup
import com.ierusalem.androchat.features_tcp.tcp.tcp.domain.TcpScreenUiState
import com.ierusalem.androchat.ui.tcp.ServerViewState
import com.ierusalem.androchat.ui.tcp.TestServerViewState

private enum class ConnectionInstructionContentTypes {
  SPACER,
}

internal fun LazyListScope.renderConnectionInstructions(
    itemModifier: Modifier = Modifier,
    appName: String,
    state: TcpScreenUiState,
    serverViewState: ServerViewState,
    onShowQRCode: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
) {
  item(
      contentType = ConnectionInstructionContentTypes.SPACER,
  ) {
    Spacer(
        modifier = Modifier.height(16.dp),
    )
  }

  renderDeviceIdentifiers(
      itemModifier = itemModifier,
  )

  item(
      contentType = ConnectionInstructionContentTypes.SPACER,
  ) {
      Spacer(
          modifier = Modifier.height(16.dp),
      )
  }

  renderAppSetup(
      itemModifier = itemModifier,
      appName = appName,
  )

  item(
      contentType = ConnectionInstructionContentTypes.SPACER,
  ) {
      Spacer(
          modifier = Modifier.height(16.dp),
      )
  }

  renderDeviceSetup(
      itemModifier = itemModifier,
      appName = appName,
      state = state,
      onTogglePasswordVisibility = onTogglePasswordVisibility,
      onShowQRCode = onShowQRCode,
      serverViewState =serverViewState
  )

  item(
      contentType = ConnectionInstructionContentTypes.SPACER,
  ) {
      Spacer(
          modifier = Modifier.height(16.dp),
      )
  }

  renderConnectionComplete(
      itemModifier = itemModifier,
      appName = appName,
  )

  item(
      contentType = ConnectionInstructionContentTypes.SPACER,
  ) {
      Spacer(
          modifier = Modifier.height(16.dp),
      )
  }
}

@Preview
@Composable
private fun PreviewConnectionInstructions() {
  LazyColumn {
    renderConnectionInstructions(
        appName = "TEST",
        state = TcpScreenUiState(),
        onTogglePasswordVisibility = {},
        onShowQRCode = {},
        serverViewState = TestServerViewState()
    )
  }
}
