package com.example.final_project_pam.viewmodel

import com.example.final_project_pam.data.model.AppUsageStats
import com.example.final_project_pam.data.model.DailyUsageStat

data class DashboardSummary(
    val totalMinutesWeek: Long,
    val averageMinutesDay: Long,
    val appCount: Int
)

data class AggregatedAppUsage(
    val packageName: String,
    val appName: String,
    val totalMinutes: Long
)

data class SeriesData(
    val packageName: String,
    val appName: String,
    val values: List<Float>
)

data class StackedChartData(
    val dayLabels: List<String>,
    val series: List<SeriesData>,
    val dateRangeLabel: String = ""
)

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(
        val userName: String,
        val usageStats: List<AppUsageStats>,
        val dailyUsageStats: List<DailyUsageStat> = emptyList(),
        val summary: DashboardSummary = DashboardSummary(0, 0, 0),
        val aggregatedApps: List<AggregatedAppUsage> = emptyList(),
        val stackedChartData: StackedChartData = StackedChartData(emptyList(), emptyList())
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}
