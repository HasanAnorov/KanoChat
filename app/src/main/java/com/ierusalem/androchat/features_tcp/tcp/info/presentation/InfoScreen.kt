package com.ierusalem.androchat.features_tcp.tcp.info.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.features_tcp.tcp.info.domain.InfoScreenUiState
import com.ierusalem.androchat.features_tcp.tcp.tcp.domain.TcpScreenUiState
import com.ierusalem.androchat.ui.tcp.ServerViewState
import com.ierusalem.androchat.ui.tcp.TestServerViewState


private enum class InfoContentTypes {
  BOTTOM_SPACER
}

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    appName: String,
    state: TcpScreenUiState,
    serverViewState: ServerViewState,
    onTogglePasswordVisibility: () -> Unit,
    onShowQRCode: () -> Unit,
) {
  LazyColumn(
      modifier = modifier,
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {

    renderConnectionInstructions(
        itemModifier = Modifier.widthIn(max = 480.dp),
        appName = appName,
        state = state,
        serverViewState = serverViewState,
        onTogglePasswordVisibility = onTogglePasswordVisibility,
        onShowQRCode = onShowQRCode,
    )

    item(
        contentType = InfoContentTypes.BOTTOM_SPACER,
    ) {
      Spacer(
          modifier = Modifier.padding(top = 16.dp).navigationBarsPadding(),
      )
    }
  }
}

@Preview
@Composable
private fun PreviewInfoScreen() {
  InfoScreen(
      appName = "TEST",
      state = TcpScreenUiState(),
      serverViewState = TestServerViewState(),
      onTogglePasswordVisibility = {},
      onShowQRCode = {},
  )
}
