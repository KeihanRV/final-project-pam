package com.example.final_project_pam.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.final_project_pam.data.model.SelectedApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "unscroll_prefs")

class AppDataStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val KEY_SELECTED_APPS = stringSetPreferencesKey("cached_selected_apps")
    }

    val cachedSelectedApps: Flow<List<SelectedApp>> = context.appDataStore.data.map { prefs ->
        prefs[KEY_SELECTED_APPS]?.mapNotNull { raw ->
            runCatching { json.decodeFromString<SelectedApp>(raw) }.getOrNull()
        } ?: emptyList()
    }

    suspend fun saveSelectedApps(apps: List<SelectedApp>) {
        context.appDataStore.edit { prefs ->
            prefs[KEY_SELECTED_APPS] = apps.map { json.encodeToString(it) }.toSet()
        }
    }

    suspend fun clearCache() {
        context.appDataStore.edit { prefs ->
            prefs.remove(KEY_SELECTED_APPS)
        }
    }
}
