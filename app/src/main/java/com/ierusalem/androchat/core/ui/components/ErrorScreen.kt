package com.ierusalem.androchat.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.features.home.presentation.contacts.ErrorType

@Composable
fun ErrorScreen(
    error: ErrorType,
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit
) {
    when (error) {
        ErrorType.NetworkError -> NetworkError(onRetryClick = onRetryClick)
        ErrorType.InvalidResponse -> InvalidResponseError(modifier = modifier)
    }
}

@Composable
fun NetworkError(onRetryClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.offline),
            contentDescription = null,
            colorFilter = ColorFilter.tint(
                MaterialTheme.colorScheme.onBackground
            )
        )
        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = "You're offline ",
            fontSize = 18.sp,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            modifier = Modifier
                .padding(top = 24.dp)
                .padding(horizontal = 16.dp),
            fontSize = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            text = "Please connect to the internet and try again",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
        )

        Button(
            modifier = Modifier.padding(24.dp),
            onClick = onRetryClick
        ) {
            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
            Text(
                text = "Retry",
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
fun InvalidResponseError(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.illustration),
            contentDescription = null
        )
        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = "Oops, Something went wrong",
            fontSize = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            modifier = Modifier
                .padding(top = 24.dp)
                .padding(horizontal = 16.dp),
            fontSize = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            text = "Please try again later.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Preview
@Composable
fun ErrorScreenPreview() {
    AndroChatTheme {
        ErrorScreen(
            error = ErrorType.NetworkError,
            onRetryClick = {}
        )
    }
}

@Preview
@Composable
fun ErrorScreenPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        ErrorScreen(
            error = ErrorType.NetworkError,
            onRetryClick = {}
        )
    }
}

@Preview
@Composable
fun InvalidResponseErrorPreview() {
    AndroChatTheme {
        InvalidResponseError()
    }
}

@Preview
@Composable
fun InvalidResponseErrorPreviewDark() {
    AndroChatTheme(isDarkTheme = true) {
        InvalidResponseError()
    }
}