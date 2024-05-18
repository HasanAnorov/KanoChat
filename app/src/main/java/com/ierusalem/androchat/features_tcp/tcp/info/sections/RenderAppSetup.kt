package com.ierusalem.androchat.features_tcp.tcp.info.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.features_tcp.server.ServerDefaults
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.TypographyDefaults

private enum class AppSetupContentTypes {
    WIFI,
    INTERNET,
    CONFIG,
    WAKELOCK,
    BATTERY,
    START,
}

internal fun LazyListScope.renderAppSetup(
    itemModifier: Modifier = Modifier,
    appName: String,
) {
    item(
        contentType = AppSetupContentTypes.WIFI,
    ) {
        ThisInstruction(
            modifier = itemModifier,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.turn_on_wi_fi),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(R.string.wifi_must_be_on, appName),
                    style =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }

    item(
        contentType = AppSetupContentTypes.INTERNET,
    ) {
        ThisInstruction(
            modifier = itemModifier.padding(top = 16.dp),
        ) {
            Column {
                Text(
                    text = stringResource(R.string.connect_internet),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(R.string.connect_internet_options),
                    style =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }

    item(
        contentType = AppSetupContentTypes.CONFIG,
    ) {
        if (ServerDefaults.canUseCustomConfig()) {
            ThisInstruction(
                modifier = itemModifier.padding(top = 16.dp),
                small = true,
            ) {
                Text(
                    text = stringResource(R.string.optionally_configure_hotspot),
                    style =
                    MaterialTheme.typography.bodyMedium.copy(
                        color =
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = TypographyDefaults.ALPHA_DISABLED,
                        ),
                    ),
                )
            }
        }
    }

    item(
        contentType = AppSetupContentTypes.WAKELOCK,
    ) {
        ThisInstruction(
            modifier = itemModifier.padding(top = MaterialTheme.keylines.content),
            small = true,
        ) {
            Text(
                text = stringResource(R.string.optionally_configure_wakelock),
                style =
                MaterialTheme.typography.bodyMedium.copy(
                    color =
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = TypographyDefaults.ALPHA_DISABLED,
                    ),
                ),
            )
        }
    }

    item(
        contentType = AppSetupContentTypes.BATTERY,
    ) {
        ThisInstruction(
            modifier = itemModifier.padding(top = MaterialTheme.keylines.content),
            small = true,
        ) {
            Text(
                text = stringResource(R.string.optionally_configure_power, appName),
                style =
                MaterialTheme.typography.bodyMedium.copy(
                    color =
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = TypographyDefaults.ALPHA_DISABLED,
                    ),
                ),
            )
        }
    }

    item(
        contentType = AppSetupContentTypes.START,
    ) {
        ThisInstruction(
            modifier = itemModifier.padding(top = MaterialTheme.keylines.content),
        ) {
            Column {
                Text(
                    text = stringResource(R.string.start_the_hotspot, appName),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(R.string.check_hotspot_green),
                    style =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewAppSetup() {
    LazyColumn {
        renderAppSetup(
            appName = "TEST",
        )
    }
}
