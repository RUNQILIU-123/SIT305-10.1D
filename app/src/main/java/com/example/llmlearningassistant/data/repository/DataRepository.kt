package com.example.llmlearningassistant.data.repository

import com.example.llmlearningassistant.data.model.Interest
import com.example.llmlearningassistant.data.model.LearningTask
import com.example.llmlearningassistant.data.model.QuizQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DataRepository {
    private val _interests = MutableStateFlow(listOf(
        Interest("1", "Algorithms"),
        Interest("2", "Data Structures"),
        Interest("3", "Web Development"),
        Interest("4", "Testing"),
        Interest("5", "Databases"),
        Interest("6", "Mobile Development"),
        Interest("7", "AI Basics")
    ))
    val interests: StateFlow<List<Interest>> = _interests

    private val _selectedInterests = MutableStateFlow<List<String>>(emptyList())
    val selectedInterests: StateFlow<List<String>> = _selectedInterests

    fun toggleInterest(interestId: String) {
        val currentList = _interests.value.map {
            if (it.id == interestId) it.copy(isSelected = !it.isSelected) else it
        }
        _interests.value = currentList
        _selectedInterests.value = currentList.filter { it.isSelected }.map { it.name }
    }

    fun getTasks(): List<LearningTask> {
        val selected = _selectedInterests.value
        if (selected.isEmpty()) return emptyList()

        return selected.mapIndexed { index, interest ->
            LearningTask(
                id = index.toString(),
                title = "Generated Task ${index + 1}: $interest Revision",
                description = "Test your knowledge on $interest concepts.",
                category = interest,
                questions = listOf(
                    QuizQuestion(
                        id = "q1",
                        questionText = "What is a primary concept in $interest?",
                        correctAnswer = "Efficiency",
                        options = listOf("Efficiency", "Randomness", "Static", "Manual")
                    ),
                    QuizQuestion(
                        id = "q2",
                        questionText = "Why is $interest important in software engineering?",
                        correctAnswer = "Scalability",
                        options = listOf("Scalability", "Coloring", "Typing Speed", "Legacy")
                    )
                )
            )
        }
    }
}
