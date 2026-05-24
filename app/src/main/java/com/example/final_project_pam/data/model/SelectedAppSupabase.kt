package com.example.final_project_pam.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SelectedAppSupabase(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("package_name")
    val packageName: String,
    @SerialName("app_label")
    val appLabel: String,
    @SerialName("unscroll_minutes")
    val unscrollMinutes: Int,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

fun SelectedAppSupabase.toDomain(): SelectedApp = SelectedApp(
    id = id,
    userId = userId,
    packageName = packageName,
    appLabel = appLabel,
    unscrollMinutes = unscrollMinutes
)
