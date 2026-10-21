package com.englishlearningapp.ui.screens.admin.users

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.englishlearningapp.core.network.getFileName
import com.englishlearningapp.ui.components.AppTopNavigationBar
import com.englishlearningapp.ui.components.AppDatePickerField

@Composable
fun UserFormScreen(
    viewModel: UserFormViewModel,
    isEditMode: Boolean,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val avatarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@runCatching
            viewModel.setAvatar(
                bytes = bytes,
                fileName = uri.getFileName(context),
                mimeType = context.contentResolver.getType(uri)
            )
        }
    }

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
                title = if (isEditMode) "Edit User" else "Create User",
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = "name" in uiState.validationErrors,
                    supportingText = { uiState.validationErrors["name"]?.let { Text(it) } }
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::updateEmail,
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = "email" in uiState.validationErrors,
                    supportingText = { uiState.validationErrors["email"]?.let { Text(it) } }
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::updatePassword,
                    label = { Text(if (isEditMode) "New Password (optional)" else "Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = "password" in uiState.validationErrors,
                    supportingText = { uiState.validationErrors["password"]?.let { Text(it) } }
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = viewModel::updatePhone,
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                AppDatePickerField(
                    value = uiState.birthday,
                    onValueChange = viewModel::updateBirthday,
                    label = "Birthday"
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.address,
                    onValueChange = viewModel::updateAddress,
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
            item {
                OutlinedButton(
                    onClick = { avatarLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(uiState.avatarFileName ?: "Select Avatar")
                }
            }
            item {
                SettingSwitchRow(
                    label = "Active account",
                    checked = uiState.status == 1,
                    onCheckedChange = viewModel::updateStatus
                )
                SettingSwitchRow(
                    label = "Super admin",
                    checked = uiState.isSuperAdmin,
                    onCheckedChange = viewModel::updateSuperAdmin
                )
            }
            item {
                Text("Roles", fontWeight = FontWeight.SemiBold)
                Text(
                    "${uiState.selectedRoleIds.size} selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(uiState.roles, key = { it.id }) { role ->
                val selected = role.id in uiState.selectedRoleIds
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleRole(role.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(
                        1.dp,
                        if (selected) MaterialTheme.colorScheme.primary else Color(0xFFE6E7EA)
                    )
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selected,
                            onCheckedChange = { viewModel.toggleRole(role.id) }
                        )
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(role.roleName, fontWeight = FontWeight.Medium)
                            if (!role.description.isNullOrBlank()) {
                                Text(
                                    role.description.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = viewModel::saveUser,
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
                        Text(if (isEditMode) "Save Changes" else "Create User")
                    }
                }
                Spacer(Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
