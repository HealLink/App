package com.heallinkapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        private val TOKEN_KEY =
            androidx.datastore.preferences.core.stringPreferencesKey("user_token")
        private val USER_NAME_KEY =
            androidx.datastore.preferences.core.stringPreferencesKey("user_name")
        private val IS_FIRST_TIME_KEY =
            androidx.datastore.preferences.core.booleanPreferencesKey("is_first_time")

        fun newInstance(context: Context): UserPreferences {
            return UserPreferences(context)
        }
    }

    suspend fun saveToken(token: String, userName: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_NAME_KEY] = userName
        }
    }

    suspend fun saveFirstTime(isFirstTime: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_TIME_KEY] = isFirstTime
        }
    }
    val userToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val isFirstTimeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_FIRST_TIME_KEY] ?: true
    }

    suspend fun clearPreferences() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_NAME_KEY)
        }
    }

}