package com.example.final_project_pam.data.source

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.final_project_pam.data.model.InstalledApp

class PackageManagerSource(private val context: Context) {

    fun getInstalledApps(): List<InstalledApp> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return try {
            val resolved = pm.queryIntentActivities(mainIntent, 0)
            val selfPackage = context.packageName

            resolved.mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                if (packageName == selfPackage) return@mapNotNull null

                val appInfo = resolveInfo.activityInfo.applicationInfo
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val label = resolveInfo.loadLabel(pm)?.toString() ?: packageName
                val isEnabled = appInfo.enabled

                InstalledApp(
                    packageName = packageName,
                    label = label,
                    isSystem = isSystem,
                    isEnabled = isEnabled,
                    hasLauncherIntent = true
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun hasLauncherIntent(packageName: String): Boolean {
        val pm = context.packageManager
        return try {
            val intent = pm.getLaunchIntentForPackage(packageName)
            intent != null
        } catch (e: Exception) {
            false
        }
    }

    fun getLaunchIntent(packageName: String): Intent? {
        return try {
            context.packageManager.getLaunchIntentForPackage(packageName)
        } catch (e: Exception) {
            null
        }
    }
}
