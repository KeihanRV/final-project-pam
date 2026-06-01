package com.example.final_project_pam.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.final_project_pam.R
import com.example.final_project_pam.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnscrollHeader(
    onBackClick: (() -> Unit)? = null,
    onLogoutClick: (() -> Unit)? = null,
    showLogoEnd: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
        shadowElevation = 4.dp,
        color = Color.White
    ) {
        TopAppBar(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!showLogoEnd) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_app),
                            contentDescription = "Logo Unscroll",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Unscroll", color = UnscrollBlack, fontWeight = FontWeight.Bold)
                }
            },
            navigationIcon = {
                if (onBackClick != null) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = UnscrollBlack
                        )
                    }
                }
            },
            actions = {
                if (showLogoEnd) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo_app),
                        contentDescription = "Logo Unscroll",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                if (onLogoutClick != null) {
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = UnscrollBlack
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
    }
}

@Composable
fun UnscrollBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            NavigationBarItem(
                selected = currentRoute == "dashboard",
                onClick = { onNavigate("dashboard") },
                icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                label = { Text("Dashboard") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = UnscrollPrimary,
                    selectedTextColor = UnscrollPrimary,
                    unselectedIconColor = UnscrollBlack.copy(alpha = 0.65f),
                    unselectedTextColor = UnscrollBlack.copy(alpha = 0.65f),
                    indicatorColor = UnscrollTertiary
                )
            )
            NavigationBarItem(
                selected = currentRoute == "app_select" || currentRoute == "app_select_picker",
                onClick = { onNavigate("app_select") },
                icon = { Icon(Icons.AutoMirrored.Outlined.List, contentDescription = "App Select") },
                label = { Text("Apps") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = UnscrollPrimary,
                    selectedTextColor = UnscrollPrimary,
                    unselectedIconColor = UnscrollBlack.copy(alpha = 0.65f),
                    unselectedTextColor = UnscrollBlack.copy(alpha = 0.65f),
                    indicatorColor = UnscrollTertiary
                )
            )
            NavigationBarItem(
                selected = currentRoute == "profile",
                onClick = { onNavigate("profile") },
                icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
                label = { Text("Profile") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = UnscrollPrimary,
                    selectedTextColor = UnscrollPrimary,
                    unselectedIconColor = UnscrollBlack.copy(alpha = 0.65f),
                    unselectedTextColor = UnscrollBlack.copy(alpha = 0.65f),
                    indicatorColor = UnscrollTertiary
                )
            )
        }
    }
}
