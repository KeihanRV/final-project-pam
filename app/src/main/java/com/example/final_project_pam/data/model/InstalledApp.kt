package com.example.final_project_pam.data.model

import kotlinx.serialization.Serializable

@Serializable
data class InstalledApp(
    val packageName: String,
    val label: String,
    val isSystem: Boolean,
    val isEnabled: Boolean,
    val hasLauncherIntent: Boolean
)
