package com.ierusalem.androchat.features_tcp.tcp.info.sections

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ierusalem.androchat.R
import com.pyamsoft.pydroid.theme.keylines

private enum class ConnectionCompleteContentTypes {
    SHARING,
    DONE,
}

internal fun LazyListScope.renderConnectionComplete(
    itemModifier: Modifier = Modifier,
    appName: String,
) {
    item(
        contentType = ConnectionCompleteContentTypes.SHARING,
    ) {
        ThisInstruction(
            modifier = itemModifier,
        ) {
            Text(
                text = stringResource(R.string.sharing_complete),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }

    item(
        contentType = ConnectionCompleteContentTypes.DONE,
    ) {
        OtherInstruction(
            modifier = itemModifier.padding(top = MaterialTheme.keylines.content),
        ) {
            Text(
                text = stringResource(R.string.sharing_caveat, appName),
                style =
                MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewConnectionComplete() {
    LazyColumn {
        renderConnectionComplete(
            appName = "TEST",
        )
    }
}
