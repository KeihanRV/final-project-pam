package com.example.final_project_pam.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.final_project_pam.features.profile.ui.screen.ProfileScreen
import com.example.final_project_pam.ui.AppPickerScreen
import com.example.final_project_pam.ui.AppSelectScreen
import com.example.final_project_pam.ui.DashboardScreen
import com.example.final_project_pam.ui.LoginScreen
import com.example.final_project_pam.ui.OTPScreen
import com.example.final_project_pam.ui.RegisterScreen
import com.example.final_project_pam.ui.components.UnscrollBottomNavigation
import com.example.final_project_pam.ui.components.UnscrollHeader
import com.example.final_project_pam.ui.theme.UnscrollBackground
import com.example.final_project_pam.ui.theme.UnscrollBlack
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

@OptIn(ExperimentalMaterial3Api::class)
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
            when (currentRoute) {
                Screen.Dashboard.route -> UnscrollHeader(onLogoutClick = { authViewModel.logout() })
                Screen.AppSelect.route -> UnscrollHeader(onBackClick = { navController.popBackStack() }, showLogoEnd = true)
                Screen.Profile.route -> UnscrollHeader(onBackClick = { navController.popBackStack() }, showLogoEnd = true)
            }
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
    val username = authViewModel.userName.collectAsStateWithLifecycle()
    val email = authViewModel.email.collectAsStateWithLifecycle()
    val password = authViewModel.password.collectAsStateWithLifecycle()
    val confirmPassword = authViewModel.confirmPassword.collectAsStateWithLifecycle()
    val otpCode = authViewModel.otpCode.collectAsStateWithLifecycle()
    val uiState = authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.value) {
        when (uiState.value) {
            is AuthUiState.OtpSent -> {
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                if (currentRoute != Screen.OTP.route) {
                    navController.navigate(Screen.OTP.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
                authViewModel.resetState()
            }
            is AuthUiState.OtpVerified -> {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
                authViewModel.resetAuthFields()
            }
            else -> {}
        }
    }

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
                onNavigateToRegister = {
                    authViewModel.resetAuthFields()
                    navController.navigate(Screen.Register.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                userName = username.value,
                email = email.value,
                password = password.value,
                confirmPassword = confirmPassword.value,
                uiState = uiState.value,
                onUsernameChange = authViewModel::onUsernameChange,
                onEmailChange = authViewModel::onEmailChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onConfirmPasswordChange = authViewModel::onConfirmPasswordChange,
                onRegisterClick = { authViewModel.register() },
                onNavigateToLogin = {
                    authViewModel.resetAuthFields()
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.OTP.route) {
            OTPScreen(
                otpCode = otpCode.value,
                uiState = uiState.value,
                onOtpChange = authViewModel::onOtpChange,
                onVerifyClick = { authViewModel.verifyOTP() },
                onResendClick = { authViewModel.sendOTP() },
                onBackClick = {
                    authViewModel.resetAuthFields()
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
