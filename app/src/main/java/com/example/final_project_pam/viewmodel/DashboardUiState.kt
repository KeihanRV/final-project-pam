package com.example.final_project_pam.viewmodel

import com.example.final_project_pam.data.model.AppUsageStats
import com.example.final_project_pam.data.model.DailyUsageStat

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(
        val userName: String,
        val usageStats: List<AppUsageStats>,
        val dailyUsageStats: List<DailyUsageStat> = emptyList()
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}
