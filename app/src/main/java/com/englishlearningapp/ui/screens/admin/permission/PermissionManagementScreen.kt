package com.englishlearningapp.ui.screens.admin.permission

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.englishlearningapp.data.remote.dto.auth.PermissionDto
import com.englishlearningapp.ui.components.AppSearchField
import com.englishlearningapp.ui.components.AppTopNavigationBar
import com.englishlearningapp.ui.screens.admin.flashcard.DeleteConfirmationDialog

@Composable
fun PermissionManagementScreen(
    viewModel: PermissionManagementViewModel,
    canCreate: Boolean,
    canUpdate: Boolean,
    canDelete: Boolean,
    onCreateClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopNavigationBar(
                title = "Manage Permissions",
                onBackClick = onBackClick,
                onRefreshClick = viewModel::refresh
            )
        },
        floatingActionButton = {
            if (canCreate) {
                FloatingActionButton(onClick = onCreateClick) {
                    Icon(Icons.Default.Add, contentDescription = "Create permission")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            AppSearchField(
                query = uiState.searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                placeholder = "Search permissions",
                onClear = viewModel::clearSearch,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                uiState.filteredPermissions.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No permissions found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            "${uiState.filteredPermissions.size} permissions",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(uiState.filteredPermissions, key = { it.id }) { permission ->
                        PermissionCard(
                            permission = permission,
                            onEditClick = if (canUpdate) ({ onEditClick(permission.id) }) else null,
                            onDeleteClick = if (canDelete) ({ viewModel.requestDelete(permission) }) else null
                        )
                    }
                }
            }
        }
    }

    uiState.deletingPermission?.let { permission ->
        DeleteConfirmationDialog(
            title = "Delete permission",
            message = "Delete '${permission.permissionName}'? This action cannot be undone.",
            isDeleting = uiState.isDeleting,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDelete
        )
    }
}

@Composable
private fun PermissionCard(
    permission: PermissionDto,
    onEditClick: (() -> Unit)?,
    onDeleteClick: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE6E7EA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(permission.permissionName, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    permission.description.orEmpty(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            onEditClick?.let { action ->
                IconButton(onClick = action) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit permission")
                }
            }
            onDeleteClick?.let { action ->
                IconButton(onClick = action) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete permission")
                }
            }
        }
    }
}
