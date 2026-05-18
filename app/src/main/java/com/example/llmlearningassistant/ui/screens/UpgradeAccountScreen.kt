package com.example.llmlearningassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.llmlearningassistant.viewmodel.MainViewModel
import com.example.llmlearningassistant.viewmodel.UiState

data class PricingPlan(val name: String, val price: String, val amount: Int, val description: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeAccountScreen(
    viewModel: MainViewModel,
    onPaymentReady: (String) -> Unit,
    onBack: () -> Unit
) {
    val paymentState by viewModel.paymentState.collectAsState()
    val profile by viewModel.userProfile.collectAsState(initial = null)
    val currentPlanName = profile?.currentPlan ?: "Starter"

    val plans = listOf(
        PricingPlan("Starter", "Free", 0, "Basic access to AI explanations."),
        PricingPlan("Intermediate", "$9.99", 999, "Unlimited AI explanations and priority access."),
        PricingPlan("Advanced", "$19.99", 1999, "Personalized flashcards and expert study tools.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upgrade Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Choose a Plan",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            plans.forEach { plan ->
                val isCurrentPlan = plan.name == currentPlanName
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = if (isCurrentPlan) 
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        else CardDefaults.cardColors()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(plan.name, style = MaterialTheme.typography.titleLarge)
                            if (isCurrentPlan) {
                                Badge(modifier = Modifier.padding(start = 8.dp)) { Text("ACTIVE") }
                            }
                        }
                        Text(plan.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            plan.price,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { 
                                if (plan.amount > 0) {
                                    viewModel.createPaymentIntent(plan.amount, plan.name)
                                } else {
                                    // 如果选择的是 Starter (Free)，直接切换无需 Stripe
                                    viewModel.onPaymentSuccess(plan.name)
                                }
                            },
                            // 如果是当前套餐，或者正在加载支付，则禁用按钮
                            enabled = !isCurrentPlan && paymentState !is UiState.Loading,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            if (paymentState is UiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(if (isCurrentPlan) "Current Plan" else "Select Plan")
                            }
                        }
                    }
                }
            }

            if (paymentState is UiState.Error) {
                Text(
                    text = (paymentState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    // 当支付意向创建成功，触发支付组件
    LaunchedEffect(paymentState) {
        if (paymentState is UiState.Success) {
            val clientSecret = (paymentState as UiState.Success<String>).data
            onPaymentReady(clientSecret)
            // 注意：这里不要立即 reset，等支付结果回来再处理
        }
    }
}
