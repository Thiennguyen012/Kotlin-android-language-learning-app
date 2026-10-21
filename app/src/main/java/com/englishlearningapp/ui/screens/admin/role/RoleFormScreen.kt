package com.englishlearningapp.ui.screens.admin.role

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.englishlearningapp.ui.components.AppSearchField
import com.englishlearningapp.ui.components.AppTopNavigationBar

@Composable
fun RoleFormScreen(
    viewModel: RoleFormViewModel,
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
                title = if (isEditMode) "Edit Role" else "Create Role",
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.roleName,
                    onValueChange = viewModel::updateRoleName,
                    label = { Text("Role Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.validationErrors.containsKey("roleName"),
                    supportingText = {
                        uiState.validationErrors["roleName"]?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }
            item {
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
            }
            item {
                Text("Permissions", fontWeight = FontWeight.SemiBold)
                Text(
                    "${uiState.selectedPermissionIds.size} selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                uiState.validationErrors["permissions"]?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(8.dp))
                AppSearchField(
                    query = uiState.permissionSearchQuery,
                    onQueryChange = viewModel::updatePermissionSearch,
                    placeholder = "Search permissions",
                    onClear = viewModel::clearPermissionSearch
                )
            }

            if (uiState.isLoadingPermissions) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(uiState.filteredPermissions, key = { it.id }) { permission ->
                    val selected = permission.id in uiState.selectedPermissionIds
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.togglePermission(permission.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(
                            1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else Color(0xFFE6E7EA)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selected,
                                onCheckedChange = { viewModel.togglePermission(permission.id) }
                            )
                            Column(Modifier.padding(start = 8.dp)) {
                                Text(permission.permissionName, fontWeight = FontWeight.Medium)
                                if (!permission.description.isNullOrBlank()) {
                                    Text(
                                        permission.description.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = viewModel::saveRole,
                    enabled = !uiState.isSaving && !uiState.isLoadingPermissions,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (isEditMode) "Save Changes" else "Create Role")
                    }
                }
                Spacer(Modifier.height(72.dp))
            }
        }
    }
}
