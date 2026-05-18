package com.example.llmlearningassistant

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.llmlearningassistant.data.local.AppDatabase
import com.example.llmlearningassistant.data.remote.NetworkModule
import com.example.llmlearningassistant.data.repository.UserRepository
import com.example.llmlearningassistant.ui.screens.*
import com.example.llmlearningassistant.viewmodel.MainViewModel
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var paymentSheet: PaymentSheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 初始化 Stripe SDK
        PaymentConfiguration.init(applicationContext, BuildConfig.STRIPE_PUBLISHABLE_KEY)

        // 2. 初始化 Database 和 Repository
        val database = AppDatabase.getDatabase(this)
        val userRepository = UserRepository(
            database.historyDao(),
            database.userProfileDao(),
            NetworkModule.stripeApiService
        )
        viewModel.initRepository(userRepository)

        // 3. 初始化 Stripe PaymentSheet
        paymentSheet = PaymentSheet(this) { result ->
            onPaymentSheetResult(result)
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel, paymentSheet)
                }
            }
        }
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show()
                viewModel.resetPaymentState()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(this, "Payment Failed: ${paymentSheetResult.error.message}", Toast.LENGTH_LONG).show()
                viewModel.resetPaymentState()
            }
            is PaymentSheetResult.Completed -> {
                Toast.makeText(this, "Payment Successful!", Toast.LENGTH_LONG).show()
                viewModel.onPaymentSuccess("Intermediate")
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel, paymentSheet: PaymentSheet) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { navController.navigate("interests") },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                viewModel = viewModel,
                onRegisterSuccess = { navController.navigate("interests") },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("interests") {
            InterestsScreen(
                viewModel = viewModel,
                onContinue = { navController.navigate("home") }
            )
        }
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onTaskClick = { task ->
                    viewModel.selectTask(task)
                    navController.navigate("task_detail")
                },
                onProfileClick = { navController.navigate("profile") }
            )
        }
        composable("profile") {
            ProfileScreen(
                viewModel = viewModel,
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToUpgrade = { navController.navigate("upgrade") },
                onBack = { navController.popBackStack() }
            )
        }

        // 深度链接路由：处理二维码扫描结果
        composable(
            route = "shared_profile?name={name}&plan={plan}&total={total}&interests={interests}",
            arguments = listOf(
                navArgument("name") { defaultValue = "Learner" },
                navArgument("plan") { defaultValue = "Starter" },
                navArgument("total") { defaultValue = "0" },
                navArgument("interests") { defaultValue = "" }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://llm-learning-assistant.com/profile?name={name}&plan={plan}&total={total}&interests={interests}"
                }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val plan = backStackEntry.arguments?.getString("plan") ?: ""
            val total = backStackEntry.arguments?.getString("total") ?: ""
            val interests = backStackEntry.arguments?.getString("interests") ?: ""
            
            SharedProfileScreen(
                name = name,
                plan = plan,
                total = total,
                interests = interests,
                onBack = { 
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate("home") 
                    }
                }
            )
        }

        composable("history") {
            HistoryScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable("upgrade") {
            UpgradeAccountScreen(
                viewModel = viewModel,
                onPaymentReady = { clientSecret ->
                    paymentSheet.presentWithPaymentIntent(
                        clientSecret,
                        PaymentSheet.Configuration("LLM Learning Assistant")
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("task_detail") {
            TaskDetailScreen(
                viewModel = viewModel,
                onNavigateToResults = { navController.navigate("results") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("results") {
            ResultsScreen(
                viewModel = viewModel,
                onContinue = { 
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    } 
                }
            )
        }
    }
}
