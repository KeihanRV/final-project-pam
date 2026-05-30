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

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                val supabase = SupabaseClientProvider.client
                val user = supabase.auth.currentUserOrNull()

                if (user != null) {
                    val metadata = user.userMetadata
                    val fullName = metadata?.get("full_name")?.jsonPrimitive?.contentOrNull
                    val userName = fullName?.split(" ")?.firstOrNull() ?: "User"

                    val stats = try {
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
                    GatewayTimerService.monitoredPackages.addAll(stats.map { it.packageName })

                    val dailyUsageStats = aggregateDailyUsage(stats)

                    _uiState.value = DashboardUiState.Success(
                        userName = userName,
                        usageStats = stats,
                        dailyUsageStats = dailyUsageStats
                    )
                } else {
                    _uiState.value = DashboardUiState.Error("User not authenticated")
                }
            } catch (e: Throwable) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Terjadi kesalahan sistem")
            }
        }
    }

    private fun aggregateDailyUsage(stats: List<AppUsageStats>): List<DailyUsageStat> {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dayNames = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")

        val days = (6 downTo 0).map { today.minusDays(it.toLong()) }

        val grouped = stats.groupBy { it.usage_date }

        return days.map { date ->
            val dateStr = date.format(dateFormatter)
            val totalMinutes = grouped[dateStr]?.sumOf { it.time_spent_minutes } ?: 0L
            DailyUsageStat(dateStr, totalMinutes)
        }
    }
}