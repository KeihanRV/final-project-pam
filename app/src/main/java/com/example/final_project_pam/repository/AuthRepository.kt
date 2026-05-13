package com.example.final_project_pam.repository

import com.example.final_project_pam.data.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow

class AuthRepository {

    /*
     * Mengambil client Supabase yang sudah dibuat sebelumnya.
     */
    private val supabase = SupabaseClientProvider.client

    /*
     * Flow untuk memantau perubahan status session (Authenticated, NotAuthenticated, dll)
     */
    val sessionStatus: Flow<SessionStatus> = supabase.auth.sessionStatus

    /*
     * Fungsi register user baru menggunakan email dan password.
     * Fungsi ini suspend karena prosesnya berjalan secara asynchronous/network.
     */
    suspend fun register(email: String, password: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /*
     * Fungsi login user menggunakan email dan password.
     */
    suspend fun login(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /*
     * Fungsi logout user dari aplikasi.
     */
    suspend fun logout() {
        supabase.auth.signOut()
    }

    /*
     * Fungsi untuk mengecek apakah user sudah login atau belum.
     * Kita buat suspend agar bisa menunggu inisialisasi Supabase selesai.
     */
    suspend fun isLoggedIn(): Boolean {
        // Menunggu Supabase selesai memuat session dari storage lokal (SharedPreferences/Settings)
        // Jika tidak ditunggu, currentSessionOrNull() mungkin masih null saat app baru dibuka.
        try {
            supabase.auth.awaitInitialization()
        } catch (e: Exception) {
            // Jika gagal inisialisasi, anggap belum login
        }

        return supabase.auth.currentSessionOrNull() != null
    }

    /*
     * Menunggu inisialisasi Auth selesai.
     */
    suspend fun awaitAuthInitialization() {
        supabase.auth.awaitInitialization()
    }
}
