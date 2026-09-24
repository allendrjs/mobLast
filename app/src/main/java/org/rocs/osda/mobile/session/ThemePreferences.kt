package org.rocs.osda.mobile.session

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "osda_preferences")

private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")

class ThemePreferences(private val context: Context) {

    val darkModeFlow: Flow<Boolean> = context.themeDataStore.data.map { it[DARK_MODE_KEY] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        context.themeDataStore.edit { it[DARK_MODE_KEY] = enabled }
    }
}
