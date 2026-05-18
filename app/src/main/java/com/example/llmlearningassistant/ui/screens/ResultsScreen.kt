package com.example.llmlearningassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.llmlearningassistant.viewmodel.MainViewModel

@Composable
fun ResultsScreen(
    viewModel: MainViewModel,
    onContinue: () -> Unit
) {
    val currentTask by viewModel.currentTask.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()

    // 10.1D: 当进入结果页时，自动将本次练习保存到本地 Room 数据库
    LaunchedEffect(currentTask) {
        currentTask?.let { task ->
            val totalQuestions = task.questions.size
            var correctCount = 0
            task.questions.forEach { q ->
                if (userAnswers[q.id].equals(q.correctAnswer, ignoreCase = true)) {
                    correctCount++
                }
            }
            
            val incorrectCount = totalQuestions - correctCount
            
            // 修正统计逻辑：按题目数量保存历史并更新统计数据
            viewModel.saveToHistory(
                title = task.title,
                type = "Quiz",
                summary = "Score: $correctCount/$totalQuestions",
                isCorrect = correctCount == totalQuestions,
                correctCount = correctCount,
                incorrectCount = incorrectCount
            )
        }
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Back to Home")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Learning Session Complete!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Results for: ${currentTask?.title ?: "the task"}",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            currentTask?.let { task ->
                items(task.questions) { question ->
                    val userAnswer = userAnswers[question.id] ?: "No answer"
                    val isCorrect = userAnswer.equals(question.correctAnswer, ignoreCase = true)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (isCorrect) Color(0xFF4CAF50) else Color.Red
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = question.questionText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Your answer: $userAnswer", style = MaterialTheme.typography.bodyMedium)
                            if (!isCorrect) {
                                Text(
                                    text = "Correct answer: ${question.correctAnswer}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
