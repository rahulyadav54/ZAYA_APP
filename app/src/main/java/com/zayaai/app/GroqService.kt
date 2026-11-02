package com.zayaai.app

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// --- Groq API Models ---

data class GroqRequestBody(
    val model: String,
    val messages: List<GroqMessageBody>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 2048
)

data class GroqMessageBody(
    val role: String,
    val content: String
)

data class GroqResponseBody(
    val choices: List<GroqChoiceBody>? = null,
    val error: GroqErrorBody? = null
)

data class GroqChoiceBody(
    val message: GroqMessageBody? = null,
    val finish_reason: String? = null
)

data class GroqErrorBody(
    val message: String? = null,
    val code: String? = null
)

interface GroqApiService {
    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: GroqRequestBody
    ): Response<GroqResponseBody>
}

// ─── Groq Service (Free, Fast, Unlimited) ───

object GroqService {
    
    private const val TAG = "GroqService"
    
    private val GROQ_API_KEY: String = BuildConfig.GROQ_API_KEY
    
    private val gson: Gson = GsonBuilder().create()
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
    
    private val groqApi: GroqApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(GroqApiService::class.java)
    }
    
    /**
     * Generate text using Groq API (fast, free, unlimited)
     * 30 requests/minute - plenty for any user!
     */
    suspend fun generate(
        prompt: String,
        systemPrompt: String? = null,
        maxTokens: Int = 2048
    ): String {
        val messages = mutableListOf<GroqMessageBody>()
        if (systemPrompt != null) {
            messages.add(GroqMessageBody("system", systemPrompt))
        }
        messages.add(GroqMessageBody("user", prompt))
        
        val request = GroqRequestBody(
            model = "mixtral-8x7b-32768",
            messages = messages,
            temperature = 0.7,
            max_tokens = maxTokens
        )
        
        return try {
                            Log.d(TAG, "Sending to Groq with key: ${GROQ_API_KEY.take(20)}...")
                            Log.d(TAG, "Request: $request")

            val response = groqApi.chatCompletion("Bearer $GROQ_API_KEY", request)
            
            Log.d(TAG, "Response code: ${response.code()}")
            Log.d(TAG, "Response body: ${response.body()}")

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.error != null) {
                            Log.e(TAG, "Groq API error: ${body.error.message}")
                    throw Exception("Groq error: ${body.error.message}")
                }
                
                val text = body?.choices?.firstOrNull()?.message?.content
                if (!text.isNullOrBlank()) {
                    Log.d(TAG, "Success from Groq")
                    return text
                }
                Log.e(TAG, "Empty response from Groq")
                throw Exception("No response from Groq")
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(TAG, "Groq HTTP ${response.code()}: $errorBody")

                throw Exception("Groq API error ${response.code()}: $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
            throw e
        }
    }
}


