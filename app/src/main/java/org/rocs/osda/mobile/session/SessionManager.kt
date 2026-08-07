package org.rocs.osda.mobile.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "osda_session")

private val TOKEN_KEY = stringPreferencesKey("token")
private val USERNAME_KEY = stringPreferencesKey("username")
private val ROLE_KEY = stringPreferencesKey("role")

class SessionManager(private val context: Context) {

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val usernameFlow: Flow<String?> = context.dataStore.data.map { it[USERNAME_KEY] }
    val roleFlow: Flow<String?> = context.dataStore.data.map { it[ROLE_KEY] }

    val studentIdFlow: Flow<String?> = usernameFlow

    suspend fun currentToken(): String? = tokenFlow.first()

    suspend fun currentStudentId(): String? = studentIdFlow.first()

    suspend fun save(token: String, username: String, role: String?) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USERNAME_KEY] = username
            if (role != null) prefs[ROLE_KEY] = role
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}