package com.example.footballstats.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.appDataStore by preferencesDataStore(name = "football_stats_prefs")
typealias AppDataStore = androidx.datastore.core.DataStore<Preferences>