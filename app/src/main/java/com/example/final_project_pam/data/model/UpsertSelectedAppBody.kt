package com.example.final_project_pam.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpsertSelectedAppBody(
    @SerialName("user_id")
    val userId: String,
    @SerialName("package_name")
    val packageName: String,
    @SerialName("app_label")
    val appLabel: String,
    @SerialName("unscroll_minutes")
    val unscrollMinutes: Int
)
