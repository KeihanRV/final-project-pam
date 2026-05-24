package com.example.final_project_pam.viewmodel

/*
 * Sealed class digunakan untuk membatasi kemungkinan state.
 * Dalam kasus auth, state-nya hanya:
 * Idle, Loading, Success, atau Error.
 */
sealed class AuthUiState {

    /*
     * Kondisi awal, belum ada proses login/register.
     */
    object Idle : AuthUiState()

    /*
     * Kondisi ketika proses login/register sedang berjalan.
     */
    object Loading : AuthUiState()

    /*
     * Kondisi ketika login/register berhasil.
     */
    object Success : AuthUiState()

    /*
     * Kondisi ketika login/register gagal.
     * message digunakan untuk menyimpan pesan error.
     */
    data class Error(val message: String) : AuthUiState()
}
