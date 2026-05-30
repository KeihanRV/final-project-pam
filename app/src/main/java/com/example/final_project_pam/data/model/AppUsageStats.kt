package com.example.final_project_pam.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AppUsageStats(
    @SerialName("id")
    val id: Long,

    @SerialName("user_id")
    val user_id: String,

    @SerialName("package_name")
    val packageName: String,

    @SerialName("app_name")
    val app_name: String,

    @SerialName("time_spent_minutes")
    val time_spent_minutes: Long,

    @SerialName("max_limit_minutes")
    val max_limit_minutes: Long,

    @SerialName("last_accessed")
    val last_accessed: String,

    @SerialName("usage_date")
    val usage_date: String = ""
)
