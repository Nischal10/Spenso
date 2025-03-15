package com.example.spenso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spenso.R
import com.example.spenso.ui.theme.SpensoTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.spenso.components.PrimaryButton
import com.example.spenso.components.SecondaryButton
import com.example.spenso.ui.theme.extendedColors
import com.example.spenso.ui.theme.fontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        // Keep splash screen visible
        var keepSplashScreenVisible = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreenVisible }

        lifecycleScope.launch {
            delay(1000) // Keep visible for 1 seconds
            keepSplashScreenVisible = false
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpensoTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .padding(bottom = 24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = "Start your Journey to Financial Freedom",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Designed to help you manage your finances easily and efficiently",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.extendedColors.text
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        PrimaryButton(
            text = "Get Started",
            onClick = { /* TODO: Handle button click */ }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        SecondaryButton(
            text = "Continue with Google",
            onClick = { /* TODO: Handle button click */ },
            iconResId = R.drawable.ic_google
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    SpensoTheme {
        MainScreen()
    }
}