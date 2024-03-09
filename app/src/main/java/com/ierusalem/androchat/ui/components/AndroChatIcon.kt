package com.ierusalem.androchat.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.ierusalem.androchat.R

/**
 * AndroChatIcon
 *
 * @author A.H.I "andro" on 07/03/2024
 */

@Composable
fun AndroChatIcon(
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val semantics = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }
    Box(modifier = modifier.then(semantics)) {
        Icon(
            painter = painterResource(id = R.drawable.ic_jetchat_back),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primaryContainer
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_jetchat_front),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
