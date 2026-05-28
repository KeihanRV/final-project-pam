package com.example.final_project_pam.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.final_project_pam.features.profile.ui.screen.ProfileScreen
import com.example.final_project_pam.ui.AppPickerScreen
import com.example.final_project_pam.ui.AppSelectScreen
import com.example.final_project_pam.ui.DashboardScreen
import com.example.final_project_pam.ui.LoginScreen
import com.example.final_project_pam.ui.RegisterScreen
import com.example.final_project_pam.ui.components.UnscrollBottomNavigation
import com.example.final_project_pam.ui.components.UnscrollHeader
import com.example.final_project_pam.ui.theme.UnscrollBackground
import com.example.final_project_pam.viewmodel.AppSelectViewModel
import com.example.final_project_pam.viewmodel.AuthCheckState
import com.example.final_project_pam.viewmodel.AuthUiState
import com.example.final_project_pam.viewmodel.AuthViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel()
) {
    val authCheckState = authViewModel.authCheckState.collectAsStateWithLifecycle()

    when (authCheckState.value) {
        is AuthCheckState.Checking -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is AuthCheckState.Authenticated -> {
            AuthenticatedLayout(authViewModel)
        }
        is AuthCheckState.NotAuthenticated -> {
            AuthNavHost(authViewModel)
        }
    }
}

@Composable
fun AuthenticatedLayout(
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val appSelectViewModel: AppSelectViewModel = viewModel()

    Scaffold(
        topBar = {
            UnscrollHeader(
                onLogoutClick = {
                    authViewModel.logout()
                }
            )
        },
        bottomBar = {
            UnscrollBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        },
        containerColor = UnscrollBackground
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen()
            }

            composable(Screen.AppSelect.route) {
                AppSelectScreen(
                    viewModel = appSelectViewModel,
                    onNavigateToPicker = { navController.navigate(Screen.AppSelectPicker.route) }
                )
            }

            composable(Screen.AppSelectPicker.route) {
                AppPickerScreen(
                    viewModel = appSelectViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        authViewModel.logout()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun AuthNavHost(
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val email = authViewModel.email.collectAsStateWithLifecycle()
    val password = authViewModel.password.collectAsStateWithLifecycle()
    val uiState = authViewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                email = email.value,
                password = password.value,
                uiState = uiState.value,
                onEmailChange = authViewModel::onEmailChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onLoginClick = { authViewModel.login() },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                email = email.value,
                password = password.value,
                uiState = uiState.value,
                onEmailChange = authViewModel::onEmailChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onRegisterClick = { authViewModel.register() },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
    }
}
