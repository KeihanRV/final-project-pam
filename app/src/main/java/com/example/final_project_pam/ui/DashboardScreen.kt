package com.example.final_project_pam.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.final_project_pam.data.model.AppUsageStats
import com.example.final_project_pam.ui.theme.*
import com.example.final_project_pam.viewmodel.DashboardUiState
import com.example.final_project_pam.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToAppSelect: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Placeholder for Unscroll Logo
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(UnscrollPrimary, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("U", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unscroll", color = UnscrollBlack, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = UnscrollBlack)
                    }
                    IconButton(onClick = { /* TODO: Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = UnscrollBlack)
                    }
                    // Logout button in TopAppBar
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
        },
        bottomBar = {
            UnscrollBottomNavigation(
                currentRoute = "dashboard",
                onDashboardClick = { },
                onAppSelectClick = onNavigateToAppSelect,
                onProfileClick = onNavigateToProfile
            )
        },
        containerColor = UnscrollBackground
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = UnscrollPrimary)
                }
                is DashboardUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DashboardUiState.Success -> {
                    DashboardContent(
                        userName = state.userName,
                        usageStats = state.usageStats,
                        onLogoutClick = onLogoutClick
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    userName: String,
    usageStats: List<AppUsageStats>,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Greeting
        Text(
            text = "Hi, $userName",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = UnscrollBlack
            )
        )
        Text(
            text = "Always look at your achievement!",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = UnscrollBlack.copy(alpha = 0.7f)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Chart Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(UnscrollPrimary, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chart",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Recent Apps Section
        Text(
            text = "Recent",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = UnscrollBlack
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(usageStats) { stat ->
                RecentAppCard(stat)
            }
            
            if (usageStats.isEmpty()) {
                item {
                    Text("No recent activity", color = UnscrollBlack.copy(alpha = 0.5f))
                }
            }
        }

//        Spacer(modifier = Modifier.weight(1f))
//
//        // Logout Button at the bottom
//        Button(
//            onClick = onLogoutClick,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 24.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = UnscrollSecondary),
//            shape = RoundedCornerShape(16.dp)
//        ) {
//            Icon(
//                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
//                contentDescription = null,
//                tint = Color.White
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
//        }
    }
}

@Composable
fun RecentAppCard(stat: AppUsageStats) {
    Card(
        modifier = Modifier.size(width = 140.dp, height = 160.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = UnscrollTertiary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Icon Placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = UnscrollSecondary)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = stat.app_name,
                fontWeight = FontWeight.Bold,
                color = UnscrollBlack,
                fontSize = 14.sp
            )
            
            Text(
                text = "2 Hours ago", // Placeholder for relative time
                color = UnscrollBlack.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun UnscrollBottomNavigation(
    currentRoute: String,
    onDashboardClick: () -> Unit,
    onAppSelectClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = UnscrollBackground,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "dashboard",
            onClick = onDashboardClick,
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = UnscrollPrimary,
                selectedTextColor = UnscrollPrimary,
                unselectedIconColor = UnscrollBlack.copy(alpha = 0.5f),
                unselectedTextColor = UnscrollBlack.copy(alpha = 0.5f),
                indicatorColor = UnscrollTertiary
            )
        )
        NavigationBarItem(
            selected = currentRoute == "app_select",
            onClick = onAppSelectClick,
            icon = { Icon(Icons.Outlined.List, contentDescription = "App Select") },
            label = { Text("Apps") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = UnscrollPrimary,
                selectedTextColor = UnscrollPrimary,
                unselectedIconColor = UnscrollBlack.copy(alpha = 0.5f),
                unselectedTextColor = UnscrollBlack.copy(alpha = 0.5f),
                indicatorColor = UnscrollTertiary
            )
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = onProfileClick,
            icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = UnscrollPrimary,
                selectedTextColor = UnscrollPrimary,
                unselectedIconColor = UnscrollBlack.copy(alpha = 0.5f),
                unselectedTextColor = UnscrollBlack.copy(alpha = 0.5f),
                indicatorColor = UnscrollTertiary
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    FinalprojectpamTheme {
        DashboardContent(
            userName = "Harvey",
            usageStats = listOf(
                AppUsageStats(1, "user1", "Instagram", 45, 60, "2023-10-27"),
                AppUsageStats(2, "user1", "TikTok", 120, 30, "2023-10-27"),
                AppUsageStats(3, "user1", "YouTube", 15, 60, "2023-10-27")
            ),
            onLogoutClick = {}
        )
    }
}
