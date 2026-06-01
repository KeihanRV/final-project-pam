package com.example.final_project_pam.viewmodel

import android.util.Patterns
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

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

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

    fun onConfirmPasswordChange(value: String) {
        _confirmPassword.value = value
    }

    fun onOtpChange(value: String) {
        _otpCode.value = value
    }

    fun login() {
        val email = _email.value.trim()
        val password = _password.value

        // Validasi input
        if (email.isEmpty()) {
            _uiState.value = AuthUiState.Error("Email tidak boleh kosong")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState.Error("Format email tidak valid")
            return
        }
        if (password.isEmpty()) {
            _uiState.value = AuthUiState.Error("Password tidak boleh kosong")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Password minimal 6 karakter")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                repository.login(email = email, password = password)
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    message = mapAuthError(e.message)
                )
            }
        }
    }

    fun register() {
        val username = _userName.value.trim()
        val email = _email.value.trim()
        val password = _password.value
        val confirmPassword = _confirmPassword.value

        // Validasi input
        if (username.isEmpty()) {
            _uiState.value = AuthUiState.Error("Nama tidak boleh kosong")
            return
        }
        if (email.isEmpty()) {
            _uiState.value = AuthUiState.Error("Email tidak boleh kosong")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState.Error("Format email tidak valid")
            return
        }
        if (password.isEmpty()) {
            _uiState.value = AuthUiState.Error("Password tidak boleh kosong")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Password minimal 6 karakter")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error("Konfirmasi password tidak cocok")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                repository.register(
                    username = username,
                    email = email,
                    password = password
                )
                _uiState.value = AuthUiState.OtpSent
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    message = mapAuthError(e.message)
                )
            }
        }
    }

    fun sendOTP() {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                repository.sendOTP(_email.value.trim())
                _uiState.value = AuthUiState.OtpSent
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(message = mapAuthError(e.message))
            }
        }
    }

    fun verifyOTP(type: OtpType.Email = OtpType.Email.SIGNUP) {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                repository.verifyOTP(
                    email = _email.value.trim(),
                    token = _otpCode.value,
                    type = type
                )
                _uiState.value = AuthUiState.OtpVerified
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(message = mapAuthError(e.message))
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

    fun resetAuthFields() {
        _userName.value = ""
        _email.value = ""
        _password.value = ""
        _confirmPassword.value = ""
        _otpCode.value = ""
        _uiState.value = AuthUiState.Idle
    }

    private fun mapAuthError(raw: String?): String {
        if (raw == null) return "Terjadi kesalahan"
        return when {
            raw.contains("Invalid login credentials", ignoreCase = true) ->
                "Email atau password salah"
            raw.contains("already registered", ignoreCase = true) ->
                "Email sudah terdaftar"
            raw.contains("Password should be at least", ignoreCase = true) ->
                "Password minimal 6 karakter"
            raw.contains("Unable to validate email address", ignoreCase = true) ->
                "Format email tidak valid"
            raw.contains("Email not confirmed", ignoreCase = true) ->
                "Email belum dikonfirmasi, cek inbox Anda"
            else -> raw
        }
    }
}
