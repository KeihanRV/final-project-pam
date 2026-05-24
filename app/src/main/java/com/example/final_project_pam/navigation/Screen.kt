package com.example.final_project_pam.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object AppSelect : Screen("app_select")
    object AppSelectPicker : Screen("app_select_picker")
    object Profile : Screen("profile")
}
