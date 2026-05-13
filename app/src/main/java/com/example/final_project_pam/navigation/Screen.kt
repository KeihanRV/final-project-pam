package com.example.final_project_pam.navigation

/*
 * Sealed class juga bisa digunakan untuk route navigasi.
 * Tujuannya agar nama route tidak ditulis manual berkali-kali.
 */
sealed class Screen(val route: String) {

    /*
     * Route untuk halaman login.
     */
    object Login : Screen("login")

    /*
     * Route untuk halaman register.
     */
    object Register : Screen("register")

    /*
     * Route untuk halaman dashboard.
     */
    object Dashboard : Screen("dashboard")
}
