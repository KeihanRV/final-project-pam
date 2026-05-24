package com.example.final_project_pam.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppUsageStats(
    val id: String,
    val user_id: String,
    val app_name: String,
    val time_spent_minutes: Long,
    val max_limit_minutes: Long,
    val last_accessed: String
)
