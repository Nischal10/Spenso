package com.example.spenso.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.spenso.R
import com.example.spenso.components.CurrencySelector
import com.example.spenso.viewmodel.CurrencyViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    user: FirebaseUser,
    onSignOut: () -> Unit,
    currencyViewModel: CurrencyViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val selectedCurrency by currencyViewModel.selectedCurrency.collectAsState()
    
    // Handle back press when drawer is open
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }
    
    // Use a key to force the drawer to be stable
    val drawerKey = remember { "currency_drawer" }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CurrencySelector(
                viewModel = currencyViewModel,
                onDismiss = { 
                    scope.launch { 
                        drawerState.close() 
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize(),
        gesturesEnabled = drawerState.isOpen,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User profile section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // User avatar
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(user.photoUrl)
                                .error(R.drawable.ic_google)
                                .build()
                        ),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // User name
                    Text(
                        text = user.displayName ?: "User",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // User email
                    Text(
                        text = user.email ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )
                }
            }
            
            // Settings list
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                SettingsItem(
                    icon = Icons.Outlined.Settings,
                    title = "Profile settings",
                    onClick = {}
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                
                SettingsItem(
                    icon = Icons.Outlined.Paid,
                    title = "Currency",
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedCurrency.code} (${selectedCurrency.symbol})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            
                            Spacer(modifier = Modifier.size(8.dp))
                            
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowRight,
                                contentDescription = "Navigate",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    },
                    onClick = {
                        // Open the drawer with proper handling for gestures
                        scope.launch { 
                            drawerState.open()
                        }
                    }
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                
                SettingsItem(
                    icon = Icons.Outlined.Palette,
                    title = "Appearance",
                    onClick = {}
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                
                SettingsItem(
                    icon = Icons.Outlined.Logout,
                    title = "Logout",
                    onClick = onSignOut,
                    tint = Color.Red
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Version info
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    trailingContent: @Composable (() -> Unit)? = {
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier
                .size(24.dp)
        )
        
        Spacer(modifier = Modifier.size(16.dp))
        
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = tint,
            modifier = Modifier.weight(1f)
        )
        
        // Trailing content (arrow icon by default)
        trailingContent?.invoke()
    }
} 