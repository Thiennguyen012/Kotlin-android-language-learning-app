package com.englishlearningapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.englishlearningapp.core.permission.PermissionUiState

@Composable
fun PermissionGuard(
    permissionName: String,
    permissionState: PermissionUiState,
    hasPermission: (String) -> Boolean,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    content: @Composable () -> Unit
) {
    when {
        permissionState.isLoading ||
            permissionState.user == null && permissionState.errorMessage == null -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        permissionState.errorMessage != null -> PermissionMessage(
            message = permissionState.errorMessage,
            primaryAction = "Try again",
            onPrimaryAction = onRetry,
            onBackClick = onBackClick
        )

        hasPermission(permissionName) -> content()

        else -> PermissionMessage(
            message = "You do not have permission to access this feature.",
            primaryAction = "Go back",
            onPrimaryAction = onBackClick
        )
    }
}

@Composable
private fun PermissionMessage(
    message: String,
    primaryAction: String,
    onPrimaryAction: () -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onPrimaryAction,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(primaryAction)
        }
        if (onBackClick != null && primaryAction != "Go back") {
            Button(
                onClick = onBackClick,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Go back")
            }
        }
    }
}
