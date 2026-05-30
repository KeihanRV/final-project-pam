package com.example.final_project_pam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.final_project_pam.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnscrollHeader(
    onLogoutClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Logo
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(UnscrollPrimary, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("U", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                // App Name
                Text("Unscroll", color = UnscrollBlack, fontWeight = FontWeight.Bold)
            }
        },
        actions = {
            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint = UnscrollBlack
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = UnscrollBackground)
    )
}

@Composable
fun UnscrollBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = UnscrollBackground,
        tonalElevation = 8.dp
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
