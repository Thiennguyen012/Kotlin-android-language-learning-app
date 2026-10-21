package com.englishlearningapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import coil.compose.AsyncImage
import com.englishlearningapp.core.network.toAssetUrl
import com.englishlearningapp.ui.theme.AppScreenBackground
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.englishlearningapp.data.remote.dto.auth.UserDto
import com.englishlearningapp.ui.components.AppTopNavigationBar

internal object ProfileColors {
    val Background = AppScreenBackground
    val Header = Color(0xFFE8F0F2)
    val PrimaryText = Color(0xFF17191C)
    val SecondaryText = Color(0xFF6F747C)
    val Accent = Color(0xFFFF9800)
}

@Composable
internal fun ProfileHeader(
    user: UserDto,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(284.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(238.dp)
                    .clip(RoundedCornerShape(bottomStart = 52.dp, bottomEnd = 52.dp))
                    .background(ProfileColors.Header)
            )
            AppTopNavigationBar(
                onBackClick = onBackClick,
                onRefreshClick = onRefreshClick,
                windowInsets = WindowInsets(0, 0, 0, 0)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(124.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color(0xFFB9E3F1),
                    shadowElevation = 2.dp
                ) {
                    AsyncImage(
                        model = user.avatar.toAssetUrl(),
                        contentDescription = "Profile avatar",
                        contentScale = ContentScale.Crop,
                        placeholder = rememberVectorPainter(Icons.Default.Person),
                        error = rememberVectorPainter(Icons.Default.Person),
                        fallback = rememberVectorPainter(Icons.Default.Person),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit profile",
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = ProfileColors.PrimaryText
        )
        Text(
            text = listOfNotNull(user.email, user.phone)
                .filter { it.isNotBlank() }
                .joinToString("  |  "),
            style = MaterialTheme.typography.bodyMedium,
            color = ProfileColors.SecondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
