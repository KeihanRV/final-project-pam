package com.example.final_project_pam.viewmodel

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    object RegisterSuccess : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
