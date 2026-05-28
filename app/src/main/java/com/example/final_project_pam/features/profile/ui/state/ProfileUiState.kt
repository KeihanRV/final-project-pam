package com.example.final_project_pam.features.profile.ui.state

import com.example.final_project_pam.features.profile.data.model.UserProfile

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
    object Deleted : ProfileUiState()
}
