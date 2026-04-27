package com.example.myphonec

import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.myphonec.ui.theme.MyPhoneCTheme
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 🔑 1. Setup Debug App Check Provider - Gọi TRƯỚC KHI sử dụng bất kỳ dịch vụ Firebase nào
        setupAppCheck()
        
        // 🚀 2. Test Firebase AI
        testGeminiAI()

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            MyPhoneCTheme {
                MainScreen()
            }
        }
    }

    private fun setupAppCheck() {
        try {
            val firebaseAppCheck = Firebase.appCheck
            if (BuildConfig.DEBUG) {
                // Cho máy ảo (Emulator)
                firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                Log.d("Firebase", "✅ Debug App Check Provider installed")
            } else {
                // Cho thiết bị thật
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
                Log.d("Firebase", "✅ Play Integrity App Check Provider installed")
            }
        } catch (e: Exception) {
            Log.e("Firebase", "❌ App Check Error: ${e.message}")
        }
    }

    private fun testGeminiAI() {
        lifecycleScope.launch {
            try {
                val aiRepository = BuildAiRepository()
                Log.d("Firebase", "🚀 Testing Gemini AI with gemini-3-flash-preview...")
                
                val result = aiRepository.suggestBuild("Xin chào")
                
                result.onSuccess { response ->
                    Log.d("Firebase", "✅ Gemini AI response: $response")
                }.onFailure { error ->
                    Log.e("Firebase", "❌ Gemini AI error: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("Firebase", "❌ Exception: ${e.message}")
            }
        }
    }
}

class AppViewModelFactory(
    private val sessionManager: SessionManager,
    private val firebaseRepository: FirebaseRepository,
    private val userBenchmarkRepository: UserBenchmarkRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val aiRepository: BuildAiRepository,
    private val authViewModelProvider: () -> AuthViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(sessionManager, firebaseRepository) as T
            }
            modelClass.isAssignableFrom(LeaderboardViewModel::class.java) -> {
                LeaderboardViewModel(leaderboardRepository) as T
            }
            modelClass.isAssignableFrom(PCBuilderViewModel::class.java) -> {
                PCBuilderViewModel(firebaseRepository, aiRepository) as T
            }
            modelClass.isAssignableFrom(BenchmarkViewModel::class.java) -> {
                BenchmarkViewModel(firebaseRepository, authViewModelProvider()) as T
            }
            modelClass.isAssignableFrom(UserProfileViewModel::class.java) -> {
                UserProfileViewModel(userBenchmarkRepository, authViewModelProvider()) as T
            }
            modelClass.isAssignableFrom(AdminViewModel::class.java) -> {
                AdminViewModel(firebaseRepository) as T
            }
            modelClass.isAssignableFrom(CompareViewModel::class.java) -> {
                CompareViewModel(firebaseRepository) as T
            }
            modelClass.isAssignableFrom(BottleneckViewModel::class.java) -> {
                BottleneckViewModel(firebaseRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val firebaseRepository = remember { FirebaseRepository() }
    val userBenchmarkRepository = remember { UserBenchmarkRepository() }
    val leaderboardRepository = remember { LeaderboardRepository() }
    val aiRepository = remember { BuildAiRepository() }
    
    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(sessionManager, firebaseRepository) as T
            }
        }
    )
    
    val appViewModelFactory = remember(authViewModel) {
        AppViewModelFactory(
            sessionManager, 
            firebaseRepository, 
            userBenchmarkRepository, 
            leaderboardRepository,
            aiRepository
        ) { authViewModel }
    }
    
    val leaderboardViewModel: LeaderboardViewModel = viewModel(factory = appViewModelFactory)
    val pcBuilderViewModel: PCBuilderViewModel = viewModel(factory = appViewModelFactory)
    val benchmarkViewModel: BenchmarkViewModel = viewModel(factory = appViewModelFactory)
    val userProfileViewModel: UserProfileViewModel = viewModel(factory = appViewModelFactory)
    val adminViewModel: AdminViewModel = viewModel(factory = appViewModelFactory)
    val compareViewModel: CompareViewModel = viewModel(factory = appViewModelFactory)
    val bottleneckViewModel: BottleneckViewModel = viewModel(factory = appViewModelFactory)
    
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val authState by authViewModel.authState.collectAsState()
    
    val startDestination = remember(authState.isLoading, authState.isLoggedIn, authState.isGuest) {
        if (authState.isLoading) null
        else if (authState.isLoggedIn || authState.isGuest) "phone"
        else "login"
    }
    
    val showBottomBar = currentRoute == "phone" || currentRoute == "pc"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xff131313)
    ) {
        if (startDestination != null) {
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
                    startDestination = startDestination,
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
                            authViewModel = authViewModel,
                            userProfileViewModel = userProfileViewModel,
                            onNavigateToDetails = { navController.navigate("device_details") },
                            onNavigateToProcessor = { navController.navigate("processor_info") },
                            onNavigateToSystemDetails = { navController.navigate("system_details") },
                            onNavigateToScreenTest = { navController.navigate("screen_test") },
                            onNavigateToSensors = { navController.navigate("sensors") },
                            onNavigateToBattery = { navController.navigate("battery_health") },
                            onNavigateToPerformance = { navController.navigate("performance") },
                            onNavigateToBenchmark = { navController.navigate("benchmark") },
                            onNavigateToLogin = { navController.navigate("login") },
                            onNavigateToLeaderboard = { navController.navigate("leaderboard") },
                            onNavigateToAdmin = { navController.navigate("admin") }
                        ) 
                    }
                    composable("pc") { 
                        PCToolsScreen(
                            modifier = Modifier.padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp),
                            onNavigateToCompare = { navController.navigate("compare_components") },
                            onNavigateToBuildPC = { navController.navigate("build_pc") },
                            onNavigateToBottleneck = { navController.navigate("bottleneck_calculator") }
                        )
                    }
                    composable("leaderboard") {
                        LeaderboardScreen(
                            onBackClick = { navController.popBackStack() },
                            viewModel = leaderboardViewModel
                        )
                    }
                    composable("benchmark") {
                        BenchmarkScreen(
                            onBackClick = { navController.popBackStack() },
                            viewModel = benchmarkViewModel
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
                                authViewModel.onContinueAsGuest()
                                navController.navigate("phone") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("admin") {
                        AdminScreen(
                            onBackClick = { navController.popBackStack() },
                            viewModel = adminViewModel
                        )
                    }
                    composable("compare_components") {
                        CompareScreen(
                            onBackClick = { navController.popBackStack() },
                            viewModel = compareViewModel
                        )
                    }
                    composable("build_pc") {
                        BuildRigScreen(
                            onBackClick = { navController.popBackStack() },
                            viewModel = pcBuilderViewModel
                        )
                    }
                    composable("bottleneck_calculator") {
                        BottleneckCalculatorScreen(
                            onBackClick = { navController.popBackStack() },
                            viewModel = bottleneckViewModel
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
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xff22d3ee))
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
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(Color(0xFF111111)),
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
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}
