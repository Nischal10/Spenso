package com.example.spenso.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spenso.data.ThemeMode
import com.example.spenso.data.ThemePreferencesRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel that manages the app's theme preferences.
 */
class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ThemePreferencesRepository(application)
    
    // Expose the theme mode as a StateFlow that will be collected in the UI
    val themeMode: StateFlow<ThemeMode> = repository.themeMode
    
    /**
     * Update the theme mode preference.
     */
    fun setThemeMode(mode: ThemeMode) {
        try {
            repository.setThemeMode(mode)
        } catch (e: Exception) {
            Log.e("ThemeViewModel", "Error saving theme preference", e)
        }
    }
} 