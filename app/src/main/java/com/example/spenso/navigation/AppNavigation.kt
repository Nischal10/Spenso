package com.example.spenso.navigation

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spenso.R
import com.example.spenso.auth.AuthState
import com.example.spenso.auth.AuthViewModel
import com.example.spenso.auth.ProfileScreen
import com.example.spenso.ui.screens.LoginScreen

object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val PROFILE_ROUTE = "profile"
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    startDestination: String = AppDestinations.LOGIN_ROUTE
) {
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    
    // Initialize Google Sign-In
    LaunchedEffect(Unit) {
        // Replace with your actual web client ID from Firebase console
        val webClientId = context.getString(R.string.default_web_client_id)
        authViewModel.initGoogleSignIn(context, webClientId)
    }
    
    // Handle navigation based on auth state
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                navController.navigate(AppDestinations.PROFILE_ROUTE) {
                    popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                }
            }
            is AuthState.Unauthenticated -> {
                if (navController.currentDestination?.route != AppDestinations.LOGIN_ROUTE) {
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {} // Handle loading and error states as needed
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onGoogleSignInClick = { launcher ->
                    authViewModel.signInWithGoogle(launcher)
                },
                onSignInResult = { intent ->
                    authViewModel.handleSignInResult(intent, context)
                }
            )
        }
        
        composable(AppDestinations.PROFILE_ROUTE) {
            val user = (authState as? AuthState.Authenticated)?.user
            if (user != null) {
                ProfileScreen(
                    user = user,
                    onSignOut = {
                        authViewModel.signOut()
                    }
                )
            }
        }
    }
} 