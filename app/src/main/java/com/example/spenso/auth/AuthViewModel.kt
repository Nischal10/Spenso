package com.example.spenso.auth

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState
    
    private lateinit var googleSignInClient: GoogleSignInClient
    
    // Token refresh constants
    private val TOKEN_REFRESH_INTERVAL = TimeUnit.HOURS.toMillis(1) // Refresh token every hour
    
    init {
        // Check if user is already signed in
        checkAuthState()
        
        // Set up token refresh listener
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                scheduleTokenRefresh(user)
            }
        }
    }
    
    private fun checkAuthState() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Check if token needs refresh
            checkAndRefreshToken(currentUser)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    private fun checkAndRefreshToken(user: FirebaseUser) {
        viewModelScope.launch {
            try {
                // Get ID token metadata
                val metadata = user.metadata
                val lastSignInTime = metadata?.lastSignInTimestamp ?: 0
                val currentTime = System.currentTimeMillis()
                
                // If token is older than refresh interval, refresh it
                if (currentTime - lastSignInTime > TOKEN_REFRESH_INTERVAL) {
                    refreshToken(user)
                } else {
                    _authState.value = AuthState.Authenticated(user)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Failed to check token: ${e.message}")
            }
        }
    }
    
    private fun scheduleTokenRefresh(user: FirebaseUser) {
        viewModelScope.launch {
            try {
                // Get token expiration time
                val metadata = user.metadata
                val lastSignInTime = metadata?.lastSignInTimestamp ?: 0
                val currentTime = System.currentTimeMillis()
                val timeToRefresh = TOKEN_REFRESH_INTERVAL - (currentTime - lastSignInTime)
                
                if (timeToRefresh > 0) {
                    // Wait until it's time to refresh
                    withContext(Dispatchers.IO) {
                        Thread.sleep(timeToRefresh)
                    }
                    refreshToken(user)
                } else {
                    refreshToken(user)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    private suspend fun refreshToken(user: FirebaseUser) {
        try {
            // Get fresh token
            val token = withContext(Dispatchers.IO) {
                user.getIdToken(true).await().token
            }
            
            if (token != null) {
                // Token refreshed successfully
                _authState.value = AuthState.Authenticated(user)
                
                // Schedule next refresh
                scheduleTokenRefresh(user)
            } else {
                // Failed to refresh token, force re-authentication
                _authState.value = AuthState.Unauthenticated
            }
        } catch (e: Exception) {
            // Handle token refresh error
            if (e.message?.contains("network error", ignoreCase = true) == true) {
                // Network error, keep user authenticated but try again later
                _authState.value = AuthState.Authenticated(user)
                
                // Try again after a delay
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        Thread.sleep(TimeUnit.MINUTES.toMillis(5))
                    }
                    refreshToken(user)
                }
            } else {
                // Other error, force re-authentication
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
    
    fun initGoogleSignIn(context: Context, webClientId: String) {
        // Using the older GoogleSignInOptions for compatibility
        // This will still show warnings but is more reliable across different devices
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }
    
    fun signInWithGoogle(launcher: ActivityResultLauncher<Intent>) {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }
    
    fun handleSignInResult(data: Intent?, context: Context) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                
                // Authenticate with Firebase using the Google account
                firebaseAuthWithGoogle(account, context)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
                Toast.makeText(context, "Authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount, context: Context) {
        try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
            
            if (user != null) {
                _authState.value = AuthState.Authenticated(user)
                // Schedule token refresh
                scheduleTokenRefresh(user)
            } else {
                _authState.value = AuthState.Error("Authentication failed")
                Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            Toast.makeText(context, "Authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
        _authState.value = AuthState.Unauthenticated
    }
    
    override fun onCleared() {
        super.onCleared()
        // Remove auth state listener when ViewModel is cleared
        auth.removeAuthStateListener { }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
} 