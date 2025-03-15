package com.example.spenso.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spenso.R
import com.example.spenso.components.PrimaryButton
import com.example.spenso.components.SecondaryButton
import com.example.spenso.ui.theme.extendedColors

@Composable
fun LoginScreen(
    onGoogleSignInClick: (ActivityResultLauncher<Intent>) -> Unit,
    onSignInResult: (Intent?) -> Unit
) {
    // Set up the Google Sign-In launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onSignInResult(result.data)
    }

    Column(
        modifier = Modifier
            .padding(16.dp) // Uniform padding of 16.dp
            .padding(bottom = 24.dp) // Additional bottom padding of 24.dp
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Start your Journey to Financial Freedom",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.Bold
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
            onClick = { /* TODO: Handle email sign-in if needed */ }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SecondaryButton(
            text = "Continue with Google",
            onClick = { onGoogleSignInClick(launcher) },
            iconResId = R.drawable.ic_google
        )
    }
}