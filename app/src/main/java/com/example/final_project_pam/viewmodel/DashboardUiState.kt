package com.example.final_project_pam.viewmodel

import com.example.final_project_pam.data.model.AppUsageStats

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(
        val userName: String,
        val usageStats: List<AppUsageStats>
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}
