package com.example.final_project_pam.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth

object SupabaseClientProvider {

    /*
     * object digunakan agar Supabase client cukup dibuat satu kali.
     * Ini mirip singleton sederhana di Kotlin.
     */
    val client = createSupabaseClient(
        supabaseUrl = "https://faxoukcqkcviuuafjugf.supabase.co",
        supabaseKey = "sb_publishable_LpHEqeR0BJG6nDuHDDVh5A_f55XydhA"
    ) {
        /*
         * install(Auth) digunakan agar aplikasi bisa memakai fitur autentikasi,
         * seperti login, register, logout, dan membaca session user.
         */
        install(Auth)
    }
}