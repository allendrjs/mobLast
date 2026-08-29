package org.rocs.osda.mobile.session

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "osda_preferences")

private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")

/**
 * The user's manual light/dark choice, set from the Appearance switch on
 * the Profile screen. Deliberately does NOT read the device's system dark
 * -mode setting (isSystemInDarkTheme()) -- this is an app-only preference,
 * independent of whatever the phone itself is set to.
 *
 * Stored in its own DataStore file, separate from SessionManager's
 * "osda_session" store, so it survives logout instead of being wiped by
 * SessionManager.clear() -- it's a display preference, not session data.
 */
class ThemePreferences(private val context: Context) {

    val darkModeFlow: Flow<Boolean> = context.themeDataStore.data.map { it[DARK_MODE_KEY] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        context.themeDataStore.edit { it[DARK_MODE_KEY] = enabled }
    }
}
