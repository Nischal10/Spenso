package com.example.spenso.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spenso.data.User
import com.example.spenso.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing user profile data.
 */
class UserViewModel : ViewModel() {
    
    private val TAG = "UserViewModel"
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()
    
    // User profile state
    private val _userProfile = MutableStateFlow<UserProfileState>(UserProfileState.Loading)
    val userProfile: StateFlow<UserProfileState> = _userProfile
    
    init {
        // Load user profile when ViewModel is created
        loadUserProfile()
        
        // Listen for auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                loadUserProfile()
                // Update last login time
                updateLastLogin(currentUser.uid)
            } else {
                _userProfile.value = UserProfileState.NotAuthenticated
            }
        }
    }
    
    /**
     * Update the last login timestamp for the user.
     */
    private fun updateLastLogin(userId: String) {
        viewModelScope.launch {
            try {
                userRepository.updateLastLogin(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating last login timestamp", e)
            }
        }
    }
    
    /**
     * Load the user profile from Firestore.
     */
    fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _userProfile.value = UserProfileState.NotAuthenticated
            return
        }
        
        _userProfile.value = UserProfileState.Loading
        
        viewModelScope.launch {
            try {
                val userExists = userRepository.userExists(currentUser.uid)
                
                if (userExists) {
                    // User exists in Firestore, fetch their data
                    val user = userRepository.getUser(currentUser.uid)
                    if (user != null) {
                        _userProfile.value = UserProfileState.Success(user)
                    } else {
                        // User document exists but couldn't be fetched
                        Log.e(TAG, "User document exists but couldn't be fetched")
                        saveUserToFirestore(currentUser)
                    }
                } else {
                    // User doesn't exist in Firestore, create them
                    Log.d(TAG, "User doesn't exist in Firestore, creating new user")
                    saveUserToFirestore(currentUser)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user profile", e)
                _userProfile.value = UserProfileState.Error("Failed to load profile: ${e.message}")
            }
        }
    }
    
    /**
     * Save the user to Firestore.
     */
    private fun saveUserToFirestore(currentUser: FirebaseUser) {
        viewModelScope.launch {
            try {
                val success = userRepository.saveUser(currentUser)
                if (success) {
                    // Successfully saved user, now fetch their data
                    val user = userRepository.getUser(currentUser.uid)
                    if (user != null) {
                        _userProfile.value = UserProfileState.Success(user)
                    } else {
                        _userProfile.value = UserProfileState.Error("Failed to load profile after saving")
                    }
                } else {
                    _userProfile.value = UserProfileState.Error("Failed to save user profile")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving user to Firestore", e)
                _userProfile.value = UserProfileState.Error("Failed to save profile: ${e.message}")
            }
        }
    }
    
    /**
     * Update the user's profile.
     */
    fun updateUserProfile(updates: Map<String, Any>) {
        val currentUser = auth.currentUser ?: return
        
        viewModelScope.launch {
            try {
                val success = userRepository.updateUserProfile(currentUser.uid, updates)
                if (success) {
                    // Reload the user profile after update
                    loadUserProfile()
                } else {
                    _userProfile.value = UserProfileState.Error("Failed to update profile")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating user profile", e)
                _userProfile.value = UserProfileState.Error("Failed to update profile: ${e.message}")
            }
        }
    }
    
    /**
     * Update the user's currency preference.
     */
    fun updateCurrency(currencyCode: String) {
        val currentUser = auth.currentUser ?: return
        
        viewModelScope.launch {
            try {
                val updates = mapOf("currency" to currencyCode)
                val success = userRepository.updateUserProfile(currentUser.uid, updates)
                if (success) {
                    // Reload the user profile after update
                    loadUserProfile()
                } else {
                    Log.e(TAG, "Failed to update currency preference")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating currency preference", e)
            }
        }
    }
}

/**
 * Sealed class representing the state of the user profile.
 */
sealed class UserProfileState {
    object Loading : UserProfileState()
    object NotAuthenticated : UserProfileState()
    data class Success(val user: User) : UserProfileState()
    data class Error(val message: String) : UserProfileState()
} 