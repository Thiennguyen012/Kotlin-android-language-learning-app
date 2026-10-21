package com.englishlearningapp.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.englishlearningapp.ui.components.AppDatePickerField

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: (String) -> Unit,
    onBackToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmationVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let(onRegisterSuccess)
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .background(Color.White)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(36.dp))
            Text("Create account", color = LoginText, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Enter your information to get started", color = LoginSecondaryText, fontSize = 15.sp)
            Spacer(Modifier.height(30.dp))

            RegisterField("Full name", uiState.name, viewModel::onNameChange, "Nguyen Van A")
            RegisterField(
                "Email Address",
                uiState.email,
                viewModel::onEmailChange,
                "hello@example.com",
                KeyboardType.Email
            )
            RegisterField("Phone", uiState.phone, viewModel::onPhoneChange, "0912345678", KeyboardType.Phone)
            LoginFieldLabel("Birthday")
            Spacer(Modifier.height(8.dp))
            AppDatePickerField(
                value = uiState.birthday,
                onValueChange = viewModel::onBirthdayChange,
                label = "Birthday",
                enabled = !uiState.isLoading
            )
            Spacer(Modifier.height(18.dp))
            RegisterField("Address", uiState.address, viewModel::onAddressChange, "Ho Chi Minh")

            PasswordField(
                label = "Password",
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                visible = passwordVisible,
                onVisibilityChange = { passwordVisible = !passwordVisible }
            )
            PasswordField(
                label = "Confirm password",
                value = uiState.passwordConfirmation,
                onValueChange = viewModel::onPasswordConfirmationChange,
                visible = confirmationVisible,
                onVisibilityChange = { confirmationVisible = !confirmationVisible }
            )

            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.register()
                },
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LoginPrimary),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create account", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }

            TextButton(
                onClick = onBackToLogin,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp)
            ) {
                Text("Already have an account? Login", color = LoginPrimary)
            }
        }
    }
}

@Composable
private fun RegisterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    LoginFieldLabel(label)
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(8.dp),
        colors = loginFieldColors(),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(18.dp))
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onVisibilityChange: () -> Unit
) {
    LoginFieldLabel(label)
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Enter your password") },
        trailingIcon = {
            IconButton(onClick = onVisibilityChange) {
                Icon(
                    imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (visible) "Hide password" else "Show password",
                    tint = LoginSecondaryText
                )
            }
        },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = RoundedCornerShape(8.dp),
        colors = loginFieldColors(),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(18.dp))
}
