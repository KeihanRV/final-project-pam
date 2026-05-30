package com.example.final_project_pam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project_pam.data.SupabaseClientProvider
import com.example.final_project_pam.data.model.AppUsageStats
import com.example.final_project_pam.data.model.DailyUsageStat
import com.example.final_project_pam.service.GatewayTimerService
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isPullToRefresh) {
                _uiState.value = DashboardUiState.Loading
            }
            _isRefreshing.value = true
            try {
                val supabase = SupabaseClientProvider.client
                val user = supabase.auth.currentUserOrNull()

                if (user != null) {
                    val metadata = user.userMetadata
                    val fullName = metadata?.get("full_name")?.jsonPrimitive?.contentOrNull
                    val userName = fullName?.split(" ")?.firstOrNull() ?: "User"

                    val allStats = try {
                        supabase.postgrest["app_usage"]
                            .select {
                                filter {
                                    eq("user_id", user.id)
                                }
                            }
                            .decodeList<AppUsageStats>()
                    } catch (e: Exception) {
                        emptyList<AppUsageStats>()
                    }

                    GatewayTimerService.monitoredPackages.clear()
                    GatewayTimerService.monitoredPackages.addAll(allStats.map { it.packageName })

                    val sevenDaysAgo = LocalDate.now().minusDays(6)
                    val stats = allStats.filter { stat ->
                        try {
                            LocalDate.parse(stat.usage_date) >= sevenDaysAgo
                        } catch (_: Exception) {
                            false
                        }
                    }

                    val dailyUsageStats = aggregateDailyUsage(stats)
                    val summary = calculateSummary(stats)
                    val aggregatedApps = aggregateByApp(stats)
                    val stackedChartData = buildStackedChartData(stats)

                    _uiState.value = DashboardUiState.Success(
                        userName = userName,
                        usageStats = allStats,
                        dailyUsageStats = dailyUsageStats,
                        summary = summary,
                        aggregatedApps = aggregatedApps,
                        stackedChartData = stackedChartData
                    )
                } else {
                    _uiState.value = DashboardUiState.Error("User not authenticated")
                }
            } catch (e: Throwable) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Terjadi kesalahan sistem")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun aggregateDailyUsage(stats: List<AppUsageStats>): List<DailyUsageStat> {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
        val grouped = stats.groupBy { it.usage_date }

        return days.map { date ->
            val dateStr = date.format(dateFormatter)
            val totalMinutes = grouped[dateStr]?.sumOf { it.time_spent_minutes } ?: 0L
            DailyUsageStat(dateStr, totalMinutes)
        }
    }

    private fun calculateSummary(stats: List<AppUsageStats>): DashboardSummary {
        val totalMinutes = stats.sumOf { it.time_spent_minutes }
        val uniqueDays = stats.map { it.usage_date }.distinct().size.coerceAtLeast(1)
        val uniqueApps = stats.map { it.packageName }.distinct().size

        return DashboardSummary(
            totalMinutesWeek = totalMinutes,
            averageMinutesDay = totalMinutes / uniqueDays,
            appCount = uniqueApps
        )
    }

    private fun aggregateByApp(stats: List<AppUsageStats>): List<AggregatedAppUsage> {
        return stats.groupBy { it.packageName }
            .map { (pkg, records) ->
                AggregatedAppUsage(
                    packageName = pkg,
                    appName = records.first().app_name,
                    totalMinutes = records.sumOf { it.time_spent_minutes }
                )
            }
            .sortedByDescending { it.totalMinutes }
    }

    private fun buildStackedChartData(stats: List<AppUsageStats>): StackedChartData {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val indoLocale = Locale("id")
        val dayNameFormatter = DateTimeFormatter.ofPattern("EEE", indoLocale)
        val indoMonths = arrayOf(
            "", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )

        val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
        val dayStrings = days.map { it.format(dateFormatter) }
        val dayLabels = days.map { it.format(dayNameFormatter) }

        val firstDay = days.first()
        val lastDay = days.last()
        val dateRangeLabel = "${firstDay.dayOfMonth} ${indoMonths[firstDay.monthValue]} - ${lastDay.dayOfMonth} ${indoMonths[lastDay.monthValue]}"

        val appGroups = stats.groupBy { it.packageName }

        val series = appGroups.map { (pkg, records) ->
            val appName = records.first().app_name
            val values = dayStrings.map { dayStr ->
                records.filter { it.usage_date == dayStr }
                    .sumOf { it.time_spent_minutes }
                    .toFloat()
            }
            SeriesData(pkg, appName, values)
        }.sortedByDescending { it.values.sum() }

        return StackedChartData(dayLabels, series, dateRangeLabel)
    }
}
