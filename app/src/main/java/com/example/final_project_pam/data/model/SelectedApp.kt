package com.example.final_project_pam.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SelectedApp(
    val id: String,
    val userId: String,
    val packageName: String,
    val appLabel: String,
    val unscrollMinutes: Int = 15,
    val lockUntilTimestamp: Long = 0L
)
