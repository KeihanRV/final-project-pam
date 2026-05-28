package com.example.final_project_pam.features.profile.data.repository

import android.util.Log
import com.example.final_project_pam.data.SupabaseClientProvider
import com.example.final_project_pam.features.profile.data.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepositoryImpl : ProfileRepository {
    private val client = SupabaseClientProvider.client

    override suspend fun getProfile(userId: String): UserProfile? = withContext(Dispatchers.IO) {
        try {
            val profile = client.postgrest["user_profile"]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<UserProfile>()
            Log.d("ProfileRepo", "Fetched profile: $profile")
            profile
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Error getting profile for userId: $userId", e)
            null
        }
    }

    override suspend fun updateProfile(profile: UserProfile): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d("ProfileRepo", "Updating profile for userId: ${profile.userId}")
            // Using upsert to handle both insert (if first time) and update
            client.postgrest["user_profile"].upsert(profile) {
                onConflict = "user_id"
            }
            Log.d("ProfileRepo", "Profile updated successfully")
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Error updating profile", e)
            throw e
        }
    }

    override suspend fun deleteProfile(userId: String): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d("ProfileRepo", "Deleting profile for userId: $userId")
            client.postgrest["user_profile"]
                .delete {
                    filter {
                        eq("user_id", userId)
                    }
                }
            client.auth.signOut()
            Log.d("ProfileRepo", "Profile data deleted and user signed out")
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Error deleting profile", e)
            throw e
        }
    }

    override suspend fun getCurrentUserId(): String? {
        try {
            client.auth.awaitInitialization()
            return client.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Error awaiting auth initialization", e)
            return null
        }
    }

    override suspend fun getCurrentUserInfo(): UserInfo? {
        try {
            client.auth.awaitInitialization()
            return client.auth.currentUserOrNull()
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Error awaiting auth initialization", e)
            return null
        }
    }

    override suspend fun uploadAvatar(userId: String, byteArray: ByteArray): String = withContext(Dispatchers.IO) {
        try {
            val fileName = "$userId.jpg"
            val bucket = client.storage["avatars"]
            
            Log.d("ProfileRepo", "Uploading avatar: $fileName")
            // Upload the file, overwrite if exists
            bucket.upload(fileName, byteArray) {
                upsert = true
            }
            
            // Get the public URL
            val url = bucket.publicUrl(fileName)
            Log.d("ProfileRepo", "Avatar uploaded successfully. Public URL: $url")
            url
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Error uploading avatar", e)
            throw e
        }
    }
}
