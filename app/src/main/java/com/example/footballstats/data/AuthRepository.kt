package com.example.footballstats.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.footballstats.core.datastore.AppDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository(
    private val dataStore: AppDataStore
) {
    private val loggedInKey = booleanPreferencesKey("logged_in")

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[loggedInKey] ?: false
    }

    suspend fun login(email: String, password: String): Boolean {
        dataStore.edit { it[loggedInKey] = true }
        return true
    }

    suspend fun logout() {
        dataStore.edit { it[loggedInKey] = false }
    }
}