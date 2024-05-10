package com.ierusalem.androchat.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.features.settings.domain.SettingsState
import com.ierusalem.androchat.ui.components.AndroChatAppBar
import com.ierusalem.androchat.ui.theme.AndroChatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    uiState: SettingsState,
    intentReducer: (SettingsScreenEvents) -> Unit,
) {
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AndroChatAppBar(
                modifier = modifier,
                scrollBehavior = scrollBehavior,
                onNavIconPressed = { intentReducer(SettingsScreenEvents.NavIconClick) },
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                actions = {
                    // Info icon
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable(onClick = { })
                            .padding(horizontal = 12.dp, vertical = 16.dp)
                            .height(24.dp),
                        contentDescription = stringResource(id = R.string.info)
                    )
                }
            )
        },
        // Exclude ime and navigation bar padding so this can be added by the UserInput composable
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            content = {
                GeneralOptionsUI()
                SupportOptionsUI()
                LogoutUi()
            }
        )
    }
}

@Composable
fun LogoutUi() {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
            .fillMaxWidth(),
        onClick = { /*TODO*/ },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 10.dp,
                        horizontal = 14.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.error,
                    text = "Logout",
                    style = MaterialTheme.typography.titleMedium
                )
                    Icon(
                        painter = painterResource(id = R.drawable.log_out),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.error
                    )
            }
        }
    )
}

@Composable
fun GeneralOptionsUI() {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 10.dp)
    ) {
        Text(
            text = "General",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        GeneralSettingsItem(
            iconStart = R.drawable.notifications,
            iconEnd = R.drawable.ic_right_arrow,
            mainText = "Notifications",
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            onClick = {}
        )
        GeneralSettingsItem(
            modifier = Modifier.padding(top = 1.dp),
            iconStart = R.drawable.language,
            iconEnd = null,
            mainText = "Language",
            subText = "English",
            onClick = {}
        )
        GeneralSettingsItem(
            modifier = Modifier.padding(top = 1.dp),
            iconStart = R.drawable.color_palette,
            iconEnd = null,
            mainText = "Theme",
            subText = "System",
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            onClick = {}
        )
    }
}

@Composable
fun SupportOptionsUI() {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 10.dp)
    ) {
        Text(
            text = "Support",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))

        GeneralSettingsItem(
            iconStart = R.drawable.ask_question,
            iconEnd = null,
            mainText = "Ask a Question",
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            onClick = {}
        )
        GeneralSettingsItem(
            modifier = Modifier.padding(top = 1.dp),
            iconStart = R.drawable.privacy,
            iconEnd = null,
            mainText = "Privacy Policy",
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            onClick = {}
        )
    }
}

@Composable
fun GeneralSettingsItem(
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.background,
    shape: RoundedCornerShape = RoundedCornerShape(0.dp),
    iconStart: Int? = null,
    iconEnd: Int? = null,
    mainText: String,
    subText: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = shape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 10.dp,
                        horizontal = 14.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1F),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    iconStart?.let {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(shape = ShapeDefaults.Medium)
                                .background(color = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                painter = painterResource(id = iconStart),
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.background,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            modifier = textModifier.weight(1F),
                            color = textColor,
                            text = mainText,
                            style = MaterialTheme.typography.titleMedium
                        )
                        subText?.let {
                            Text(
                                text = subText,
                                color = Color.Blue,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
                iconEnd?.let {
                    Icon(
                        painter = painterResource(id = iconEnd),
                        contentDescription = "",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    )
}

@Preview
@Composable
private fun SettingsScreenPreviewLight() {
    AndroChatTheme {
        SettingsScreen(
            modifier = Modifier,
            uiState = SettingsState(),
            intentReducer = {}
        )
    }
}

@Preview
@Composable
private fun SettingsScreenPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        SettingsScreen(
            modifier = Modifier,
            uiState = SettingsState(),
            intentReducer = {}
        )
    }
}