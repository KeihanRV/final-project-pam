package com.example.final_project_pam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project_pam.data.SupabaseClientProvider
import com.example.final_project_pam.data.model.AppUsageStats
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
                    val userName = user.userMetadata?.get("full_name")?.toString()?.split(" ")?.firstOrNull() ?: "Harvey"
                    
                    val stats = supabase.postgrest["app_usage"]
                        .select {
                            filter {
                                eq("user_id", user.id)
                            }
                        }
                        .decodeList<AppUsageStats>()

                    _uiState.value = DashboardUiState.Success(
                        userName = userName,
                        usageStats = stats
                    )
                } else {
                    _uiState.value = DashboardUiState.Error("User not authenticated")
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}
