package com.englishlearningapp.ui.screens.admin.users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.englishlearningapp.ui.components.AppTopNavigationBar

@Composable
fun UserDetailScreen(
    viewModel: UserDetailViewModel,
    canUpdate: Boolean,
    onEditClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopNavigationBar(
                title = "User Details",
                onBackClick = onBackClick,
                onRefreshClick = viewModel::refresh,
                actions = {
                    if (canUpdate) uiState.user?.let { user ->
                        IconButton(onClick = { onEditClick(user.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit user")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            uiState.errorMessage != null -> Column(
                Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(uiState.errorMessage.orEmpty())
                Button(onClick = viewModel::refresh, modifier = Modifier.padding(top = 12.dp)) {
                    Text("Try again")
                }
            }
            uiState.user != null -> {
                val user = uiState.user!!
                Column(
                    Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE6E7EA))
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text(user.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(16.dp))
                            UserInfoRow("Email", user.email)
                            UserInfoRow("Phone", user.phone.orEmpty())
                            UserInfoRow("Birthday", user.birthday.orEmpty().substringBefore("T"))
                            UserInfoRow("Address", user.address.orEmpty())
                            UserInfoRow("Status", if (user.status == 1) "Active" else "Inactive")
                            UserInfoRow("Super admin", if (user.isSuperAdmin) "Yes" else "No")
                            UserInfoRow("Roles", user.roleIds.joinToString().ifBlank { "None" })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserInfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value.ifBlank { "—" }, fontWeight = FontWeight.Medium)
    }
}
