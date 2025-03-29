package com.example.spenso.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.spenso.data.Currency
import com.example.spenso.data.CurrencyData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CurrencyViewModel : ViewModel() {
    private val _selectedCurrency = MutableStateFlow(CurrencyData.defaultCurrency)
    val selectedCurrency = _selectedCurrency.asStateFlow()
    
    // For filtering in search
    var searchQuery by mutableStateOf("")
        private set
    
    val filteredCurrencies get() = if (searchQuery.isBlank()) {
        CurrencyData.currencies
    } else {
        CurrencyData.currencies.filter { currency ->
            currency.name.contains(searchQuery, ignoreCase = true) ||
            currency.country.contains(searchQuery, ignoreCase = true) ||
            currency.code.contains(searchQuery, ignoreCase = true)
        }
    }
    
    fun updateSearchQuery(query: String) {
        searchQuery = query
    }
    
    fun selectCurrency(currency: Currency) {
        _selectedCurrency.value = currency
    }
    
    fun isCurrencySelected(currencyId: String): Boolean {
        return _selectedCurrency.value.id == currencyId
    }
} 