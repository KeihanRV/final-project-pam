package com.example.final_project_pam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project_pam.data.SupabaseClientProvider
import com.example.final_project_pam.data.model.AppUsageStats
import com.example.final_project_pam.service.GatewayTimerService
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

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
                    // Mengambil metadata dengan cara yang lebih aman untuk menghindari error Serializable
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

                    // Update daftar package yang dimonitor
                    GatewayTimerService.monitoredPackages.clear()
                    GatewayTimerService.monitoredPackages.addAll(stats.map { it.packageName })

                    _uiState.value = DashboardUiState.Success(
                        userName = userName,
                        usageStats = stats
                    )
                } else {
                    _uiState.value = DashboardUiState.Error("User not authenticated")
                }
            } catch (e: Throwable) {
                // Menggunakan Throwable untuk mem-bypass error classpath pada class Exception
                _uiState.value = DashboardUiState.Error(e.message ?: "Terjadi kesalahan sistem")
            }
        }
    }
}