package com.example.final_project_pam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.final_project_pam.navigation.AppNavigation
import com.example.final_project_pam.ui.theme.FinalprojectpamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinalprojectpamTheme {
                AppNavigation()
            }
        }
    }
}