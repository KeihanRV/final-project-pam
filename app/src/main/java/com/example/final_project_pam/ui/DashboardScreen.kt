package com.example.final_project_pam.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    onLogoutClick: () -> Unit
) {
    /*
     * Dashboard sederhana yang muncul setelah user berhasil login.
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Anda berhasil login ke aplikasi."
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogoutClick
        ) {
            Text("Logout")
        }
    }
}
