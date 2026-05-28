package com.example.final_project_pam.features.profile.data.repository

import com.example.final_project_pam.features.profile.data.model.UserProfile
import io.github.jan.supabase.auth.user.UserInfo

interface ProfileRepository {
    suspend fun getProfile(userId: String): UserProfile?
    suspend fun updateProfile(profile: UserProfile)
    suspend fun deleteProfile(userId: String)
    suspend fun getCurrentUserId(): String?
    suspend fun getCurrentUserInfo(): UserInfo?
    suspend fun uploadAvatar(userId: String, byteArray: ByteArray): String
}
