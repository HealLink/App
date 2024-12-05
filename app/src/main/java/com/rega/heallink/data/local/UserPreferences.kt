package com.rega.heallink.data.local

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

    val userToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }


    suspend fun clearPreferences() {
        context.dataStore.edit { it.clear() }
    }
}