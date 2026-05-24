package com.example.final_project_pam.viewmodel

import com.example.final_project_pam.data.model.InstalledApp
import com.example.final_project_pam.data.model.SelectedApp

sealed interface AppSelectUiState {
    object Loading : AppSelectUiState
    data class Success(
        val selectedApps: List<SelectedApp>,
        val installedApps: List<InstalledApp> = emptyList(),
        val isRefreshing: Boolean = false,
        val isSaving: Boolean = false,
        val pendingActionPackage: String? = null,
        val snackbarMessage: String? = null
    ) : AppSelectUiState
    data class Error(
        val message: String,
        val selectedApps: List<SelectedApp> = emptyList(),
        val isRetrying: Boolean = false
    ) : AppSelectUiState
}
