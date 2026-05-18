package com.example.llmlearningassistant.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

data class PaymentRequest(val amount: Int, val planName: String)
data class PaymentResponse(val clientSecret: String)

interface StripeApiService {
    @POST("create-payment-intent")
    suspend fun createPaymentIntent(@Body request: PaymentRequest): PaymentResponse
}
