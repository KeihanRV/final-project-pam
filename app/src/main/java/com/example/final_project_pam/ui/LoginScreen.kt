package com.example.final_project_pam.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.final_project_pam.navigation.Screen

@Composable
fun LoginScreen(navController: NavController) {
    Column {
        Text(text = "Login Screen")
        Button(onClick = { navController.navigate(Screen.Dashboard.route) }) {
            Text(text = "Go to Dashboard")
        }
        Button(onClick = { navController.navigate(Screen.Register.route) }) {
            Text(text = "Go to Register")
        }
    }
}