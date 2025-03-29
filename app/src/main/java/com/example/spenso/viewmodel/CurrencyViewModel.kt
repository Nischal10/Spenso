package com.example.spenso.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spenso.data.Currency
import com.example.spenso.data.CurrencyData
import com.example.spenso.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CurrencyViewModel : ViewModel() {
    private val _selectedCurrency = MutableStateFlow(CurrencyData.defaultCurrency)
    val selectedCurrency = _selectedCurrency.asStateFlow()
    
    // Expose the full currency list
    val currencies = CurrencyData.currencies
    
    // For filtering in search
    var searchQuery by mutableStateOf("")
        private set
    
    // User repository to get user currency preferences
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()
    
    init {
        // Load the user's preferred currency from Firestore
        loadUserCurrency()
    }
    
    private fun loadUserCurrency() {
        val currentUser = auth.currentUser ?: return
        
        viewModelScope.launch {
            try {
                val user = userRepository.getUser(currentUser.uid)
                if (user != null && user.currency.isNotBlank()) {
                    // Find the currency in our list
                    val userCurrency = currencies.find { it.code == user.currency }
                    if (userCurrency != null) {
                        _selectedCurrency.value = userCurrency
                    }
                }
            } catch (e: Exception) {
                // If there's an error, we'll keep the default currency
            }
        }
    }
    
    val filteredCurrencies get() = if (searchQuery.isBlank()) {
        currencies
    } else {
        currencies.filter { currency ->
            currency.name.contains(searchQuery, ignoreCase = true) ||
            currency.country.contains(searchQuery, ignoreCase = true) ||
            currency.code.contains(searchQuery, ignoreCase = true)
        }
    }
    
    fun updateSearchQuery(query: String) {
        searchQuery = query
    }
    
    fun selectCurrency(currency: Currency) {
        // Update local state immediately
        _selectedCurrency.value = currency
        
        // Save the selected currency to the user's preferences in Firestore
        saveUserCurrency(currency.code)
    }
    
    private fun saveUserCurrency(currencyCode: String) {
        val currentUser = auth.currentUser ?: return
        
        viewModelScope.launch {
            try {
                // First update the local state (already done in selectCurrency)
                
                // Then update Firestore
                val success = userRepository.updateUserProfile(currentUser.uid, mapOf("currency" to currencyCode))
                if (!success) {
                    Log.e("CurrencyViewModel", "Failed to save currency preference to Firestore")
                } else {
                    Log.d("CurrencyViewModel", "Currency preference saved successfully: $currencyCode")
                }
            } catch (e: Exception) {
                Log.e("CurrencyViewModel", "Error saving currency preference", e)
            }
        }
    }
    
    fun isCurrencySelected(currencyId: String): Boolean {
        return _selectedCurrency.value.id == currencyId
    }
} 