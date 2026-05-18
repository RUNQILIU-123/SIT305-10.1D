package com.example.llmlearningassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.llmlearningassistant.BuildConfig
import com.example.llmlearningassistant.data.model.HistoryItem
import com.example.llmlearningassistant.data.model.LLMResponse
import com.example.llmlearningassistant.data.model.LearningTask
import com.example.llmlearningassistant.data.model.UserProfileEntity
import com.example.llmlearningassistant.data.remote.NetworkModule
import com.example.llmlearningassistant.data.repository.DataRepository
import com.example.llmlearningassistant.data.repository.LLMRepository
import com.example.llmlearningassistant.data.repository.LLMRepositoryImpl
import com.example.llmlearningassistant.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class MainViewModel : ViewModel() {
    private val dataRepository = DataRepository()
    private lateinit var userRepository: UserRepository
    private var isRepoInitialized = false

    // State for the logged-in user
    private val _currentUsername = MutableStateFlow<String?>(null)
    val currentUsername: StateFlow<String?> = _currentUsername.asStateFlow()

    fun initRepository(repo: UserRepository) {
        this.userRepository = repo
        isRepoInitialized = true
    }

    // Call this upon login or registration success
    fun onUserAuthenticated(username: String, email: String) {
        _currentUsername.value = username
        userRepository.setCurrentUser(username)
        viewModelScope.launch {
            userRepository.initProfileIfEmpty(username, email)
        }
    }

    private val llmRepository: LLMRepository = LLMRepositoryImpl(
        apiService = NetworkModule.apiService,
        apiKey = BuildConfig.OPENAI_API_KEY
    )

    private val _llmState = MutableStateFlow<UiState<LLMResponse>>(UiState.Idle)
    val llmState: StateFlow<UiState<LLMResponse>> = _llmState.asStateFlow()

    // History and Profile now react to the current user
    val history: Flow<List<HistoryItem>> 
        get() = if (isRepoInitialized) userRepository.getHistory() else emptyFlow()
        
    val userProfile: Flow<UserProfileEntity?> 
        get() = if (isRepoInitialized) userRepository.getUserProfile() else emptyFlow()

    private val _paymentState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val paymentState: StateFlow<UiState<String>> = _paymentState.asStateFlow()

    val interests = dataRepository.interests
    val selectedInterests = dataRepository.selectedInterests

    private val _currentTask = MutableStateFlow<LearningTask?>(null)
    val currentTask: StateFlow<LearningTask?> = _currentTask.asStateFlow()

    private val _userAnswers = MutableStateFlow<Map<String, String>>(emptyMap())
    val userAnswers: StateFlow<Map<String, String>> = _userAnswers.asStateFlow()

    fun toggleInterest(id: String) {
        dataRepository.toggleInterest(id)
    }

    fun getTasks() = dataRepository.getTasks()

    fun selectTask(task: LearningTask) {
        _currentTask.value = task
        _userAnswers.value = emptyMap()
        _llmState.value = UiState.Idle
    }

    fun setUserAnswer(questionId: String, answer: String) {
        val current = _userAnswers.value.toMutableMap()
        current[questionId] = answer
        _userAnswers.value = current
    }

    fun explainAnswer(question: String, correctAnswer: String, userAnswer: String) {
        viewModelScope.launch {
            _llmState.value = UiState.Loading
            llmRepository.explainAnswer(question, correctAnswer, userAnswer)
                .onSuccess { 
                    _llmState.value = UiState.Success(it)
                    saveToHistory("Explanation", "LLM Feedback", "Explained why '$userAnswer' was analyzed.")
                }
                .onFailure { _llmState.value = UiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun generateFlashcards(topic: String) {
        viewModelScope.launch {
            _llmState.value = UiState.Loading
            llmRepository.generateFlashcards(topic)
                .onSuccess { 
                    _llmState.value = UiState.Success(it)
                    saveToHistory("Flashcards", "Study Tool", "Generated flashcards for $topic")
                }
                .onFailure { _llmState.value = UiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun saveToHistory(
        title: String, 
        type: String, 
        summary: String, 
        isCorrect: Boolean? = null,
        correctCount: Int = 0,
        incorrectCount: Int = 0
    ) {
        viewModelScope.launch {
            if (isRepoInitialized) {
                userRepository.saveHistory(title, type, summary, isCorrect)
                if (correctCount > 0 || incorrectCount > 0) {
                    userRepository.updateStats(correctCount, incorrectCount)
                } else if (isCorrect != null) {
                    userRepository.updateStats(if (isCorrect) 1 else 0, if (isCorrect) 0 else 1)
                }
            }
        }
    }

    fun createPaymentIntent(amount: Int, planName: String) {
        viewModelScope.launch {
            if (isRepoInitialized) {
                _paymentState.value = UiState.Loading
                userRepository.createPaymentIntent(amount, planName)
                    .onSuccess { _paymentState.value = UiState.Success(it) }
                    .onFailure { _paymentState.value = UiState.Error(it.message ?: "Payment Setup Failed") }
            }
        }
    }

    fun onPaymentSuccess(newPlan: String) {
        viewModelScope.launch {
            if (isRepoInitialized) {
                userRepository.updatePlan(newPlan)
                _paymentState.value = UiState.Idle
            }
        }
    }

    fun resetLlmState() {
        _llmState.value = UiState.Idle
    }
    
    fun resetPaymentState() {
        _paymentState.value = UiState.Idle
    }
}
