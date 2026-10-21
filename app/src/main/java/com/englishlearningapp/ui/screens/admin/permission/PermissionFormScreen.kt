package com.englishlearningapp.ui.screens.admin.permission

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.englishlearningapp.ui.components.AppTopNavigationBar

@Composable
fun PermissionFormScreen(
    viewModel: PermissionFormViewModel,
    isEditMode: Boolean,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            uiState.successMessage?.let { snackbarHostState.showSnackbar(it) }
            onSaveSuccess()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopNavigationBar(
                title = if (isEditMode) "Edit Permission" else "Create Permission",
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.permissionName,
                onValueChange = viewModel::updatePermissionName,
                label = { Text("Permission Name") },
                placeholder = { Text("permission.view") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.validationErrors.containsKey("permissionName"),
                supportingText = {
                    uiState.validationErrors["permissionName"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                isError = uiState.validationErrors.containsKey("description"),
                supportingText = {
                    uiState.validationErrors["description"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = viewModel::savePermission,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isEditMode) "Save Changes" else "Create Permission")
                }
            }
        }
    }
}
