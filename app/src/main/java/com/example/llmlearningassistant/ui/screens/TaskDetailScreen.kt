package com.example.llmlearningassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.llmlearningassistant.ui.components.LLMDisplay
import com.example.llmlearningassistant.ui.components.LoadingView
import com.example.llmlearningassistant.ui.components.ErrorView
import com.example.llmlearningassistant.viewmodel.MainViewModel
import com.example.llmlearningassistant.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    viewModel: MainViewModel,
    onNavigateToResults: () -> Unit,
    onBack: () -> Unit
) {
    val task by viewModel.currentTask.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()
    val llmState by viewModel.llmState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(task?.title ?: "Task Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<") // Simplified back button icon
                    }
                }
            )
        }
    ) { padding ->
        if (task == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No task selected")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(task!!.description, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(24.dp))

            task!!.questions.forEach { question ->
                Text(question.questionText, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                question.options.forEach { option ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(
                            selected = userAnswers[question.id] == option,
                            onClick = { viewModel.setUserAnswer(question.id, option) }
                        )
                        Text(option)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val q = task!!.questions.first()
                        viewModel.explainAnswer(q.questionText, q.correctAnswer, userAnswers[q.id] ?: "")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Explain My Answer")
                }
                
                Button(
                    onClick = { viewModel.generateFlashcards(task!!.category) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Create Flashcards")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = llmState) {
                is UiState.Loading -> LoadingView("AI is thinking...")
                is UiState.Error -> ErrorView(state.message) { viewModel.resetLlmState() }
                is UiState.Success -> {
                    LLMDisplay(prompt = state.data.prompt, response = state.data.response)
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onNavigateToResults,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Full Results")
            }
        }
    }
}
