package com.example.llmlearningassistant.data.repository

import com.example.llmlearningassistant.data.local.HistoryDao
import com.example.llmlearningassistant.data.local.UserProfileDao
import com.example.llmlearningassistant.data.model.HistoryItem
import com.example.llmlearningassistant.data.model.UserProfileEntity
import com.example.llmlearningassistant.data.remote.PaymentRequest
import com.example.llmlearningassistant.data.remote.StripeApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class UserRepository(
    private val historyDao: HistoryDao,
    private val profileDao: UserProfileDao,
    private val stripeApi: StripeApiService
) {
    private var currentUsername: String? = null

    fun setCurrentUser(username: String) {
        currentUsername = username
    }

    fun getHistory(): Flow<List<HistoryItem>> {
        return currentUsername?.let { historyDao.getHistoryForUser(it) } ?: emptyFlow()
    }

    fun getUserProfile(): Flow<UserProfileEntity?> {
        return currentUsername?.let { profileDao.getUserProfile(it) } ?: emptyFlow()
    }

    suspend fun saveHistory(title: String, type: String, summary: String, isCorrect: Boolean?) {
        currentUsername?.let {
            historyDao.insertHistory(
                HistoryItem(
                    username = it,
                    title = title,
                    type = type,
                    summary = summary,
                    isCorrect = isCorrect
                )
            )
        }
    }
    
    suspend fun initProfileIfEmpty(username: String, email: String) {
        val existing = profileDao.getUserProfileSync(username)
        if (existing == null) {
            profileDao.insertOrUpdateProfile(
                UserProfileEntity(
                    username = username,
                    email = email
                )
            )
        }
    }

    suspend fun updatePlan(plan: String) {
        currentUsername?.let { profileDao.updatePlan(it, plan) }
    }
    
    suspend fun updateStats(correct: Int, incorrect: Int) {
        currentUsername?.let { profileDao.updateStats(it, correct, incorrect) }
    }

    suspend fun createPaymentIntent(amount: Int, planName: String): Result<String> {
        return try {
            val response = stripeApi.createPaymentIntent(PaymentRequest(amount, planName))
            Result.success(response.clientSecret)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
