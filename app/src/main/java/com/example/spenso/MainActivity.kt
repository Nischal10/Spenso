package com.example.spenso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.spenso.navigation.AppNavigation
import com.example.spenso.ui.theme.SpensoTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        // Keep splash screen visible
        var keepSplashScreenVisible = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreenVisible }

        lifecycleScope.launch {
            delay(1000) // Keep visible for 1 second
            keepSplashScreenVisible = false
        }
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            SpensoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}