package com.example.myphonec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.myphonec.ui.theme.MyPhoneCTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            MyPhoneCTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val authViewModel: AuthViewModel = viewModel()
    
    val showBottomBar = currentRoute == "phone" || currentRoute == "pc"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xff131313)
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    BottomNavigationBar(navController)
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "phone",
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    )
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    )
                }
            ) {
                composable("phone") { 
                    MyPhoneScreen(
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                        onNavigateToDetails = { navController.navigate("device_details") },
                        onNavigateToProcessor = { navController.navigate("processor_info") },
                        onNavigateToSystemDetails = { navController.navigate("system_details") },
                        onNavigateToScreenTest = { navController.navigate("screen_test") },
                        onNavigateToSensors = { navController.navigate("sensors") },
                        onNavigateToBattery = { navController.navigate("battery_health") },
                        onNavigateToPerformance = { navController.navigate("performance") },
                        onNavigateToLogin = { navController.navigate("login") }
                    ) 
                }
                composable("pc") { 
                    Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                        PCToolsScreen()
                    }
                }
                composable("login") {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onLoginSuccess = { 
                            navController.navigate("phone") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onSkipLogin = { 
                            navController.navigate("phone") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }
                composable("device_details") { 
                    DeviceDetailsScreen(onBackClick = { navController.popBackStack() }) 
                }
                composable("processor_info") { 
                    ProcessorInfoScreen(onBackClick = { navController.popBackStack() })
                }
                composable("system_details") {
                    SystemDetailsScreen(onBackClick = { navController.popBackStack() })
                }
                composable("screen_test") {
                    ScreenTestScreen(onBackClick = { navController.popBackStack() })
                }
                composable("sensors") {
                    SensorsScreen(onBackClick = { navController.popBackStack() })
                }
                composable("battery_health") {
                    BatteryHealthScreen(onBackClick = { navController.popBackStack() })
                }
                composable("performance") {
                    PerformanceScreen(onBackClick = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: androidx.navigation.NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = Color.Black.copy(alpha = 0.9f),
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0),
        modifier = Modifier
            .padding(start = 48.dp, end = 48.dp, bottom = 24.dp)
            .navigationBarsPadding()
            .height(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(30.dp))
    ) {
        val items = listOf(
            Triple("phone", "PHONE", R.drawable.phone),
            Triple("pc", "PC", R.drawable.container)
        )

        items.forEach { (route, label, iconRes) ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == route } == true
            
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = { 
                    Text(
                        text = label, 
                        style = TextStyle(
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xff22d3ee),
                    selectedTextColor = Color(0xff22d3ee),
                    unselectedIconColor = Color(0xff71717a),
                    unselectedTextColor = Color(0xff71717a),
                    indicatorColor = Color(0xff22d3ee).copy(alpha = 0.12f)
                )
            )
        }
    }
}
