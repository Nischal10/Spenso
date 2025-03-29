package com.example.spenso.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spenso.data.ThemeMode
import com.example.spenso.data.ThemePreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel that manages the app's theme preferences.
 */
class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ThemePreferencesRepository(application)
    
    // Expose the theme mode as a StateFlow that will be collected in the UI
    val themeMode: StateFlow<ThemeMode> = repository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.SYSTEM
        )
    
    /**
     * Update the theme mode preference.
     */
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }
} 