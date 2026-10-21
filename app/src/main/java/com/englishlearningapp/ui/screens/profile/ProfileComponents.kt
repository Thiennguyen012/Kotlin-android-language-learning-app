package com.englishlearningapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.englishlearningapp.data.remote.dto.auth.UserDto

@Composable
internal fun AccountInformationCard(
    user: UserDto,
    onEditClick: () -> Unit
) {
    ProfileSectionCard {
        ProfileMenuRow(
            icon = Icons.Default.ManageAccounts,
            title = "Edit profile information",
            onClick = onEditClick,
            showArrow = true
        )
        ProfileDivider()
        ProfileMenuRow(
            icon = Icons.Default.Email,
            title = "Email",
            value = user.email
        )
        ProfileDivider()
        ProfileMenuRow(
            icon = Icons.Default.Phone,
            title = "Phone",
            value = user.phone ?: "Not set"
        )
    }
}

@Composable
internal fun PersonalInformationCard(user: UserDto) {
    ProfileSectionCard {
        ProfileMenuRow(
            icon = Icons.Default.Cake,
            title = "Birthday",
            value = formatBirthday(user.birthday)
        )
        ProfileDivider()
        ProfileMenuRow(
            icon = Icons.Default.Home,
            title = "Address",
            value = user.address ?: "Not set"
        )
    }
}

@Composable
internal fun LogoutCard(
    isLoading: Boolean,
    onLogoutClick: () -> Unit
) {
    ProfileSectionCard {
        ProfileMenuRow(
            icon = Icons.AutoMirrored.Filled.Logout,
            title = "Logout",
            titleColor = MaterialTheme.colorScheme.error,
            enabled = !isLoading,
            onClick = onLogoutClick,
            trailingContent = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            }
        )
    }
}

@Composable
private fun ProfileSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    value: String? = null,
    valueColor: Color = ProfileColors.PrimaryText,
    titleColor: Color = ProfileColors.PrimaryText,
    enabled: Boolean = true,
    showArrow: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(enabled = enabled, onClick = onClick)
    } else {
        Modifier
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickModifier)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = titleColor
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = titleColor,
            modifier = Modifier.weight(1f)
        )
        trailingContent?.invoke()
        value?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = valueColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier.width(150.dp)
            )
        }
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = ProfileColors.SecondaryText
            )
        }
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = Color(0xFFE7E7E7)
    )
}

@Composable
internal fun ProfileErrorBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(14.dp)
        )
    }
}

private fun formatBirthday(birthday: String?): String {
    if (birthday.isNullOrBlank()) return "Not set"
    return birthday.substringBefore("T")
}
