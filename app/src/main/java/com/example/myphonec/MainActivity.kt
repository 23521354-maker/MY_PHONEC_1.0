package com.example.myphonec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
        color = Color(0xff131313) // App background remains gray
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
                        modifier = Modifier.padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp),
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
                    PCToolsScreen(
                        modifier = Modifier.padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp),
                        onNavigateToCompare = { navController.navigate("compare_components") }
                    )
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
                composable("compare_components") {
                    CompareScreen(onBackClick = { navController.popBackStack() })
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black) // Black area covering bottom and safe area
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(Color(0xFF111111)), // Pill Container background
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                Triple("phone", "PHONE", R.drawable.phone),
                Triple("pc", "PC", R.drawable.container)
            )

            items.forEach { (route, label, iconRes) ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == route } == true
                
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .width(110.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(if (isSelected) Color(0xff22d3ee).copy(alpha = 0.15f) else Color.Transparent)
                        .clickable {
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
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            tint = if (isSelected) Color(0xff22d3ee) else Color(0xff71717a),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = label,
                            color = if (isSelected) Color(0xff22d3ee) else Color(0xff71717a),
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }
        }
        // This ensures the area under the floating dock is also black (respecting system nav bar)
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}
