package com.example.spenso.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cottage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.spenso.R
import com.example.spenso.auth.AuthState
import com.example.spenso.auth.AuthViewModel
import com.example.spenso.ui.screens.CategoryScreen
import com.example.spenso.ui.screens.HomeScreen
import com.example.spenso.ui.screens.LoginScreen
import com.example.spenso.ui.screens.ProfileScreen
import com.example.spenso.ui.screens.TransactionsScreen
import com.example.spenso.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseUser

object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val MAIN_ROUTE = "main"
}

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String?) {
    data object Home : BottomNavItem("home", Icons.Default.Cottage, "Home")
    data object Transactions : BottomNavItem("transactions", Icons.Default.Receipt, "Transactions")
    data object Add : BottomNavItem("add", Icons.Default.Add, null)
    data object Category : BottomNavItem("category", Icons.Default.Sell, "Categories")
    data object Profile : BottomNavItem("profile", Icons.Default.Person, "You")
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Transactions,
    BottomNavItem.Add,
    BottomNavItem.Category,
    BottomNavItem.Profile
)

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
                navController.navigate(AppDestinations.MAIN_ROUTE) {
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
        
        composable(AppDestinations.MAIN_ROUTE) {
            val user = (authState as? AuthState.Authenticated)?.user
            if (user != null) {
                MainScreen(
                    user = user,
                    onSignOut = {
                        authViewModel.signOut()
                    },
                    userViewModel = viewModel()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    user: FirebaseUser,
    onSignOut: () -> Unit,
    userViewModel: UserViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    Scaffold(
        topBar = {
            val currentRoute = currentDestination?.route ?: BottomNavItem.Home.route
            val title = bottomNavItems.find { it.route == currentRoute }?.title ?: "Home"
            
            // Hide title for profile screen
            if (currentRoute != BottomNavItem.Profile.route) {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    
                    NavigationBarItem(
                        icon = { 
                            if (item == BottomNavItem.Profile) {
                                // User profile image for Profile tab
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            ImageRequest.Builder(LocalContext.current)
                                                .data(user.photoUrl)
                                                .error(R.drawable.ic_google)
                                                .build()
                                        ),
                                        contentDescription = "Profile",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            } else if (item == BottomNavItem.Add) {
                                // Circular background for Add button
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = "Add",
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                // Regular icon for other tabs
                                Icon(
                                    imageVector = item.icon, 
                                    contentDescription = item.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        label = {
                            item.title?.let {
                                Text(
                                    text = it,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        },
                        selected = selected,
                        onClick = {
                            if (currentDestination?.route != item.route) {
                                navController.navigate(item.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen()
            }
            composable(BottomNavItem.Transactions.route) {
                TransactionsScreen()
            }
            composable(BottomNavItem.Add.route) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Add new transaction")
                }
            }
            composable(BottomNavItem.Category.route) {
                CategoryScreen()
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    user = user, 
                    onSignOut = onSignOut,
                    userViewModel = userViewModel
                )
            }
        }
    }
} 