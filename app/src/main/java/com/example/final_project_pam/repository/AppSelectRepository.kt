package com.example.final_project_pam.repository

import android.content.Context
import com.example.final_project_pam.data.SupabaseClientProvider
import com.example.final_project_pam.data.local.AppDataStore
import com.example.final_project_pam.data.model.InstalledApp
import com.example.final_project_pam.data.model.SelectedApp
import com.example.final_project_pam.data.model.SelectedAppSupabase
import com.example.final_project_pam.data.model.UpsertSelectedAppBody
import com.example.final_project_pam.data.model.toDomain
import com.example.final_project_pam.data.source.PackageManagerSource
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.first

class AppSelectRepository(context: Context) {

    private val supabase = SupabaseClientProvider.client
    private val packageManagerSource = PackageManagerSource(context)
    private val appDataStore = AppDataStore(context)

    suspend fun getUserId(): String? {
        return try {
            supabase.auth.awaitInitialization()
            supabase.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }

    fun getInstalledApps(): List<InstalledApp> {
        return packageManagerSource.getInstalledApps()
            .sortedBy { it.label.lowercase() }
    }

    suspend fun getUserSelectedApps(uid: String): List<SelectedApp> {
        val fromNetwork = try {
            supabase.postgrest["user_selected_apps"]
                .select {
                    filter {
                        eq("user_id", uid)
                    }
                }
                .decodeList<SelectedAppSupabase>()
                .map { it.toDomain() }
        } catch (e: Exception) {
            null
        }

        if (fromNetwork != null) {
            appDataStore.saveSelectedApps(fromNetwork)
            return fromNetwork
        }

        return appDataStore.cachedSelectedApps.first()
    }

    suspend fun upsertSelectedApp(
        uid: String,
        packageName: String,
        appLabel: String,
        unscrollMinutes: Int
    ): SelectedApp? {
        return try {
            val body = UpsertSelectedAppBody(
                userId = uid,
                packageName = packageName,
                appLabel = appLabel,
                unscrollMinutes = unscrollMinutes
            )

            val existing = supabase.postgrest["user_selected_apps"]
                .select {
                    filter {
                        eq("user_id", uid)
                        eq("package_name", packageName)
                    }
                }
                .decodeSingleOrNull<SelectedAppSupabase>()

            if (existing != null) {
                supabase.postgrest["user_selected_apps"]
                    .update({
                        set("app_label", appLabel)
                        set("unscroll_minutes", unscrollMinutes)
                    }) {
                        filter {
                            eq("id", existing.id)
                        }
                    }
                existing.copy(
                    appLabel = appLabel,
                    unscrollMinutes = unscrollMinutes
                ).toDomain()
            } else {
                val inserted = supabase.postgrest["user_selected_apps"]
                    .insert(body)
                    .decodeSingle<SelectedAppSupabase>()
                inserted.toDomain()
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteSelectedApp(uid: String, packageName: String): Boolean {
        return try {
            supabase.postgrest["user_selected_apps"]
                .delete {
                    filter {
                        eq("user_id", uid)
                        eq("package_name", packageName)
                    }
                }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateCachedApps(apps: List<SelectedApp>) {
        appDataStore.saveSelectedApps(apps)
    }

    fun getLaunchIntent(packageName: String) = packageManagerSource.getLaunchIntent(packageName)

    fun hasLauncherIntent(packageName: String) = packageManagerSource.hasLauncherIntent(packageName)
}
