package com.example.spenso.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Theme options supported by the app.
 */
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
 * Repository for managing theme preferences using SharedPreferences.
 * 
 * This implementation uses SharedPreferences instead of DataStore to avoid
 * the unresolved reference issues with DataStore.
 */
class ThemePreferencesRepository(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )
    
    private val _themeMode = MutableStateFlow(getStoredThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode
    
    /**
     * Get the stored theme mode from SharedPreferences.
     */
    private fun getStoredThemeMode(): ThemeMode {
        val themeName = sharedPreferences.getString(THEME_MODE_KEY, ThemeMode.SYSTEM.name)
        return ThemeMode.fromString(themeName)
    }
    
    /**
     * Set the theme mode.
     */
    fun setThemeMode(mode: ThemeMode) {
        sharedPreferences.edit().putString(THEME_MODE_KEY, mode.name).apply()
        _themeMode.value = mode
    }
    
    companion object {
        private const val PREFERENCES_NAME = "theme_preferences"
        private const val THEME_MODE_KEY = "theme_mode"
    }
} 