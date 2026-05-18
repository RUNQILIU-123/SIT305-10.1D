package com.example.llmlearningassistant.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_items")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String, // Tie history to a specific username
    val title: String,
    val type: String,
    val summary: String,
    val isCorrect: Boolean? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val username: String, // Use username as unique ID
    val email: String,
    val currentPlan: String = "Starter",
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0
)
