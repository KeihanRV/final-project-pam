package com.example.final_project_pam.features.profile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Data model matching the 'user_profile' table in Supabase.
 */
@Serializable
data class UserProfile(
    @SerialName("id")
    val id: JsonElement? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("avatar")
    val avatar: String? = null,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    @SerialName("birth_date")
    val birthDate: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("user_email")
    val userEmail: String? = null
)
