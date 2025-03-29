package com.example.spenso.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property for accessing the DataStore from the Context
val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

// Theme options
enum class ThemeMode {
    SYSTEM, LIGHT, DARK;
    
    companion object {
        fun fromString(value: String?): ThemeMode {
            return when (value) {
                LIGHT.name -> LIGHT
                DARK.name -> DARK
                else -> SYSTEM
            }
        }
    }
}

/**
 * Repository for managing theme preferences using DataStore.
 */
class ThemePreferencesRepository(private val context: Context) {
    
    private val themeDataStore = context.themeDataStore
    
    // Key for storing theme preference
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    
    // Get the current theme mode as a Flow
    val themeMode: Flow<ThemeMode> = themeDataStore.data.map { preferences ->
        ThemeMode.fromString(preferences[THEME_MODE_KEY])
    }
    
    // Set the theme mode
    suspend fun setThemeMode(mode: ThemeMode) {
        themeDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }
} 