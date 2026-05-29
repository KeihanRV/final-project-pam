package com.example.final_project_pam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project_pam.repository.AuthRepository
import io.github.jan.supabase.auth.OtpType
import kotlinx.coroutines.flow.MutableStateFlow
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _authCheckState = MutableStateFlow<AuthCheckState>(AuthCheckState.Checking)
    val authCheckState: StateFlow<AuthCheckState> = _authCheckState

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _otpCode = MutableStateFlow("")
    val otpCode: StateFlow<String> = _otpCode

    init {
        observeAuthStatus()
    }

    private fun observeAuthStatus() {
        viewModelScope.launch {
            repository.sessionStatus.collect { status ->
                _authCheckState.value = when (status) {
                    is SessionStatus.Authenticated -> AuthCheckState.Authenticated
                    is SessionStatus.NotAuthenticated -> AuthCheckState.NotAuthenticated
                    is SessionStatus.Initializing -> AuthCheckState.Checking
                    is SessionStatus.RefreshFailure -> {
                        if (repository.isLoggedIn()) AuthCheckState.Authenticated
                        else AuthCheckState.NotAuthenticated
                    }
                }
            }
        }
    }

    fun onUsernameChange(value: String) {
        _userName.value = value
    }

    fun onEmailChange(value: String) {
        _email.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun onOtpChange(value: String) {
        _otpCode.value = value
    }

    fun login() {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                repository.login(
                    email = _email.value,
                    password = _password.value
                )
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(message = e.message ?: "Login gagal")
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                repository.register(
                    username = _userName.value,
                    email = _email.value,
                    password = _password.value
                )
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(message = e.message ?: "Register gagal")
            }
        }
    }

    /**
     * Mengirim OTP ke email untuk login (Magic Link/OTP)
     */
    fun sendOTP() {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                repository.sendOTP(_email.value)
                _uiState.value = AuthUiState.Success // Bisa digunakan untuk navigasi ke layar input OTP
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(message = e.message ?: "Gagal mengirim OTP")
            }
        }
    }

    /**
     * Verifikasi OTP yang dimasukkan user
     */
    fun verifyOTP(type: OtpType.Email = OtpType.Email.SIGNUP) {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                repository.verifyOTP(
                    email = _email.value,
                    token = _otpCode.value,
                    type = type
                )
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(message = e.message ?: "OTP salah atau kadaluarsa")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
