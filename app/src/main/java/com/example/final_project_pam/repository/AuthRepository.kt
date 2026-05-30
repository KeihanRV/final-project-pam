package com.example.final_project_pam.repository

import com.example.final_project_pam.data.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.OtpType
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {

    private val supabase = SupabaseClientProvider.client

    val sessionStatus: Flow<SessionStatus> = supabase.auth.sessionStatus

    /**
     * Register dengan Email & Password, serta menyimpan metadata username.
     * Supabase akan mengirimkan email konfirmasi secara otomatis.
     */
    suspend fun register(username: String, email: String, password: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("full_name", username)
            }
        }
    }

    /**
     * Login menggunakan Email & Password.
     */
    suspend fun login(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /**
     * Mengirim OTP ke Email untuk login tanpa password atau verifikasi.
     */
    suspend fun sendOTP(email: String) {
        supabase.auth.signInWith(OTP) {
            this.email = email
        }
    }

    /**
     * Verifikasi kode OTP yang diterima melalui email.
     */
    suspend fun verifyOTP(email: String, token: String, type: OtpType.Email = OtpType.Email.SIGNUP) {
        supabase.auth.verifyEmailOtp(
            type = type,
            email = email,
            token = token
        )
    }

    suspend fun logout() {
        supabase.auth.signOut()
    }

    suspend fun isLoggedIn(): Boolean {
        try {
            supabase.auth.awaitInitialization()
        } catch (e: Exception) {}
        return supabase.auth.currentSessionOrNull() != null
    }
}
