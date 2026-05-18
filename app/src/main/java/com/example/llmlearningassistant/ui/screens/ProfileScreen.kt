package com.example.llmlearningassistant.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.llmlearningassistant.ui.components.QRCodeGenerator
import com.example.llmlearningassistant.viewmodel.MainViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToUpgrade: () -> Unit,
    onBack: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState(initial = null)
    val selectedInterests by viewModel.selectedInterests.collectAsState()
    val context = LocalContext.current
    var showQrDialog by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = profile?.username ?: "Loading...", style = MaterialTheme.typography.headlineMedium)
            Text(text = profile?.email ?: "", style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Account Statistics", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Questions: ${profile?.totalQuestions ?: 0}")
                    val accuracy = if ((profile?.totalQuestions ?: 0) > 0) (profile?.correctAnswers ?: 0) * 100 / (profile?.totalQuestions ?: 1) else 0
                    Text("Accuracy: $accuracy%")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Current Plan: ${profile?.currentPlan ?: "Starter"}", color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val username = profile?.username ?: "Learner"
                    val plan = profile?.currentPlan ?: "Starter"
                    val total = profile?.totalQuestions ?: 0
                    val interests = selectedInterests.joinToString(", ")
                    
                    // Generate plain text content that any QR scanner can read
                    val qrContent = """
                        LLM Learning Assistant Profile
                        User: $username
                        Plan: $plan
                        Total Questions: $total
                        Interests: $interests
                    """.trimIndent()
                    
                    qrBitmap = QRCodeGenerator.generateQRCode(qrContent)
                    showQrDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Generate Sharing QR Code")
            }

            OutlinedButton(onClick = onNavigateToHistory, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("View Permanent History")
            }
            
            Button(
                onClick = onNavigateToUpgrade,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Upgrade Account")
            }
        }
    }

    if (showQrDialog && qrBitmap != null) {
        Dialog(onDismissRequest = { showQrDialog = false }) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Share Profile QR Code", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Image(
                        bitmap = qrBitmap!!.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(250.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            val uri = saveBitmapToCache(context, qrBitmap!!)
                            if (uri != null) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share QR Code"))
                            }
                        }) {
                            Text("Share")
                        }
                        TextButton(onClick = { showQrDialog = false }) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

// Helper function to save the QR code bitmap to cache and get a shareable URI
private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "profile_qr_code.png")
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.close()
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
