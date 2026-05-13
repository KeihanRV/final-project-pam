package com.example.final_project_pam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project_pam.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    /*
     * Repository digunakan untuk mengakses Supabase.
     * Untuk materi dasar, repository dibuat langsung di ViewModel.
     *
     * Pada project besar, lebih baik gunakan Dependency Injection seperti Hilt.
     */
    private val repository = AuthRepository()

    /*
     * _uiState bersifat private agar hanya ViewModel yang bisa mengubah state.
     */
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)

    /*
     * uiState bersifat public agar UI hanya bisa membaca state,
     * tetapi tidak bisa mengubah langsung.
     */
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _authCheckState = MutableStateFlow<AuthCheckState>(AuthCheckState.Checking)
    val authCheckState: StateFlow<AuthCheckState> = _authCheckState

    /*
     * State untuk input email.
     * Disimpan di ViewModel agar tetap aman saat recomposition.
     */
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    /*
     * State untuk input password.
     */
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    init {
        observeAuthStatus()
    }

    /*
     * Fungsi untuk memantau status autentikasi secara real-time.
     * Supabase akan otomatis memuat session dari storage saat app dibuka.
     */
    private fun observeAuthStatus() {
        viewModelScope.launch {
            repository.sessionStatus.collect { status ->
                _authCheckState.value = when (status) {
                    is SessionStatus.Authenticated -> AuthCheckState.Authenticated
                    is SessionStatus.NotAuthenticated -> AuthCheckState.NotAuthenticated
                    is SessionStatus.Initializing -> AuthCheckState.Checking
                    is SessionStatus.RefreshFailure -> {
                        // Jika refresh gagal (misal koneksi internet), tetap cek session yang ada
                        // atau anggap tidak terautentikasi jika session expired.
                        if (repository.isLoggedIn()) AuthCheckState.Authenticated
                        else AuthCheckState.NotAuthenticated
                    }
                }
            }
        }
    }

    /*
     * Fungsi ini dipanggil dari UI ketika user mengetik email.
     */
    fun onEmailChange(value: String) {
        _email.value = value
    }

    /*
     * Fungsi ini dipanggil dari UI ketika user mengetik password.
     */
    fun onPasswordChange(value: String) {
        _password.value = value
    }

    /*
     * Fungsi login.
     * viewModelScope digunakan agar coroutine mengikuti lifecycle ViewModel.
     */
    fun login() {
        viewModelScope.launch {
            try {
                /*
                 * Ubah state menjadi Loading agar UI bisa menampilkan progress.
                 */
                _uiState.value = AuthUiState.Loading

                /*
                 * Panggil repository untuk login ke Supabase.
                 */
                repository.login(
                    email = _email.value,
                    password = _password.value
                )

                /*
                 * Jika berhasil, ubah state menjadi Success.
                 */
                _uiState.value = AuthUiState.Success

            } catch (e: Exception) {
                /*
                 * Jika gagal, tampilkan pesan error.
                 */
                _uiState.value = AuthUiState.Error(
                    message = e.message ?: "Login gagal"
                )
            }
        }
    }

    /*
     * Fungsi register user baru.
     */
    fun register() {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading

                repository.register(
                    email = _email.value,
                    password = _password.value
                )

                _uiState.value = AuthUiState.Success

            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    message = e.message ?: "Register gagal"
                )
            }
        }
    }

    /*
     * Fungsi logout.
     */
    fun logout() {
        viewModelScope.launch {
            repository.logout()

            /*
             * Setelah logout, state dikembalikan ke Idle.
             */
            _uiState.value = AuthUiState.Idle
        }
    }

    /*
     * Fungsi ini digunakan untuk mengembalikan state ke Idle.
     * Biasanya dipanggil setelah navigasi berhasil.
     */
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
