package com.example.final_project_pam.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.final_project_pam.navigation.Screen

@Composable
fun DashboardScreen(navController: NavController) {
    Column {
        Text(text = "Dashboard Screen")
        Button(onClick = { 
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Dashboard.route) { inclusive = true }
            }
        }) {
            Text(text = "Logout")
        }
    }
}