package com.example.final_project_pam.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppUsageInsertBody(
    @SerialName("user_id")
    val userId: String,
    @SerialName("package_name")
    val packageName: String,
    @SerialName("app_name")
    val appName: String,
    @SerialName("time_spent_minutes")
    val timeSpentMinutes: Long,
    @SerialName("max_limit_minutes")
    val maxLimitMinutes: Long,
    @SerialName("last_accessed")
    val lastAccessed: String,
    @SerialName("usage_date")
    val usageDate: String
)
