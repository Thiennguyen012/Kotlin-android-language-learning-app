package com.englishlearningapp.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.englishlearningapp.ui.components.AppTopNavigationBar
import com.englishlearningapp.ui.components.AppDatePickerField
import com.englishlearningapp.core.network.toAssetUrl
import com.englishlearningapp.core.network.getFileName
import coil.compose.AsyncImage

/**
 * Edit Profile screen with editable fields for name, phone, birthday, address.
 * Email is displayed as read-only.
 * TODO: Add avatar upload field when multipart image upload is implemented.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel,
    onSaveSuccess: () -> Unit,
    onCancelClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val avatarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@runCatching
            viewModel.onAvatarSelected(
                bytes = bytes,
                fileName = uri.getFileName(context),
                mimeType = context.contentResolver.getType(uri),
                previewUri = uri.toString()
            )
        }
    }
    // Navigate back when save succeeds
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaveSuccess()
        }
    }

    // Show error in snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopNavigationBar(
                title = "Edit Profile",
                onBackClick = onCancelClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(112.dp),
                    shape = CircleShape,
                    color = Color(0xFFB9E3F1),
                    shadowElevation = 2.dp
                ) {
                    AsyncImage(
                        model = uiState.avatarPreviewUri ?: uiState.avatarPath.toAssetUrl(),
                        contentDescription = "Avatar preview",
                        contentScale = ContentScale.Crop,
                        placeholder = rememberVectorPainter(Icons.Default.Person),
                        error = rememberVectorPainter(Icons.Default.Person),
                        fallback = rememberVectorPainter(Icons.Default.Person),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { avatarLauncher.launch("image/*") },
                    enabled = !uiState.isSaving
                ) {
                    Text(if (uiState.avatarBytes == null) "Choose avatar" else "Change avatar")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Email (read-only)
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { /* Read only */ },
                label = { Text("Email") },
                readOnly = true,
                enabled = false,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Name (editable, required)
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Name *") },
                singleLine = true,
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { error ->
                    { Text(error, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Phone (editable, required)
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text("Phone *") },
                singleLine = true,
                isError = uiState.phoneError != null,
                supportingText = uiState.phoneError?.let { error ->
                    { Text(error, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppDatePickerField(
                value = uiState.birthday,
                onValueChange = viewModel::onBirthdayChange,
                label = "Birthday *",
                error = uiState.birthdayError,
                enabled = !uiState.isSaving
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Address (editable, required)
            OutlinedTextField(
                value = uiState.address,
                onValueChange = viewModel::onAddressChange,
                label = { Text("Address *") },
                singleLine = false,
                maxLines = 3,
                isError = uiState.addressError != null,
                supportingText = uiState.addressError?.let { error ->
                    { Text(error, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Action Buttons ---
            Row(modifier = Modifier.fillMaxWidth()) {
                // Cancel button
                OutlinedButton(
                    onClick = onCancelClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    enabled = !uiState.isSaving
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Save button
                Button(
                    onClick = { viewModel.saveProfile() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
