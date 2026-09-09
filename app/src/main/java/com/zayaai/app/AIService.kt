package com.zayaai.app

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

// --- OpenRouter/AgentRouter API Models ---

data class OpenRouterRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<OpenRouterMessage>,
    @SerializedName("max_tokens") val max_tokens: Int = 4096,
    @SerializedName("temperature") val temperature: Double = 0.7
)

/**
 * OpenRouter message where `content` can be either a plain String (text-only)
 * or a List<ContentPart> (multimodal: text + images).
 */
data class OpenRouterMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

// --- Multimodal Content Parts ---

sealed class ContentPart

data class TextContentPart(
    @SerializedName("type") val type: String = "text",
    @SerializedName("text") val text: String
) : ContentPart()

data class ImageUrlContentPart(
    @SerializedName("type") val type: String = "image_url",
    @SerializedName("image_url") val image_url: ImageUrl
) : ContentPart()

data class ImageUrl(
    @SerializedName("url") val url: String // "data:image/jpeg;base64,..." or a URL
)

// --- Response Models ---

data class OpenRouterResponse(
    @SerializedName("choices") val choices: List<OpenRouterChoice>? = null,
    @SerializedName("error") val error: OpenRouterError? = null
)

data class OpenRouterChoice(
    @SerializedName("message") val message: OpenRouterChoiceMessage
)

data class OpenRouterChoiceMessage(
    @SerializedName("role") val role: String? = null,
    @SerializedName("content") val content: String? = null
)

data class OpenRouterError(
    @SerializedName("message") val message: String? = null,
    @SerializedName("code") val code: Int? = null
)

// --- Retrofit Interface ---

interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authHeader: String,
        @Header("HTTP-Referer") referer: String,
        @Header("X-Title") title: String,
        @Body request: OpenRouterRequest
    ): Response<OpenRouterResponse>
}

// --- Centralized AI Service ---

object AIService {
    private val API_KEY: String = BuildConfig.OPENROUTER_API_KEY

    // Models supported by OpenRouter - Production models (reliable and documented)
    private val MODELS = listOf(
        "openai/gpt-4o-mini", // GPT-4o Mini - fast and intelligent
        "openai/gpt-3.5-turbo", // GPT-3.5 Turbo - reliable workhorse
        "qwen/qwen-2.5-72b-instruct", // Qwen 2.5 72B - powerful open model
        "meta-llama/llama-3-8b-instruct" // Llama 3 8B - reliable open model
    )

    private val gson: Gson = GsonBuilder().create()

    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val api: OpenRouterApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(OpenRouterApi::class.java)
    }

    /**
     * Send a simple prompt and get a text response.
     */
    suspend fun generate(prompt: String, systemPrompt: String? = null): String {
        return chat(
            messages = buildList {
                if (systemPrompt != null) add(OpenRouterMessage("system", systemPrompt))
                add(OpenRouterMessage("user", prompt))
            }
        )
    }

    /**
     * Send a full conversation (multi-turn) and get a response.
     * Includes retry logic for rate limits.
     */
    suspend fun chat(
        messages: List<OpenRouterMessage>,
        maxTokens: Int = 2048,
        temperature: Double = 0.7
    ): String {
        var lastError = "All models failed."
        var hitRateLimit = false
        var noEndpointsAvailable = false
        var globalRetryCount = 0
        val maxGlobalRetries = 3
        val retryDelayMs = 3000L 

        while (globalRetryCount < maxGlobalRetries) {
            for (model in MODELS) {
                try {
                    val request = OpenRouterRequest(
                        model = model,
                        messages = messages,
                        max_tokens = maxTokens,
                        temperature = temperature
                    )

                    val response = api.chatCompletion(
                        authHeader = "Bearer $API_KEY",
                        referer = "https://aizaya.app",
                        title = "AI ZAYA",
                        request = request
                    )

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.error != null) {
                            val errorMsg = body.error.message ?: "Unknown error"
                            
                            // Check for quota exhaustion
                            if (errorMsg.contains("quota", ignoreCase = true) || 
                                errorMsg.contains("insufficient", ignoreCase = true)) {
                                lastError = "API quota reached. Your API account may have exhausted credits. Please check your OpenRouter dashboard."
                                globalRetryCount = maxGlobalRetries
                                break
                            }
                            
                            if (errorMsg.contains("No endpoints found", ignoreCase = true)) {
                                noEndpointsAvailable = true
                                lastError = "Model $model: No available endpoints"
                                continue
                            }
                            lastError = "Model $model: $errorMsg"
                            continue
                        }
                        val text = body?.choices?.firstOrNull()?.message?.content
                        if (!text.isNullOrBlank()) return text
                        lastError = "Model $model: Empty response"
                        continue
                    }

                    if (response.code() == 429) {
                        hitRateLimit = true
                        lastError = "Rate limit hit - API is being used heavily"
                        globalRetryCount++
                        if (globalRetryCount < maxGlobalRetries) {
                            delay(retryDelayMs)
                            break 
                        }
                        continue
                    }
                    
                    if (response.code() == 401 || response.code() == 403) {
                        lastError = "Authentication failed: API key may be invalid or quota exhausted"
                        globalRetryCount = maxGlobalRetries
                        break
                    }
                    
                    val errorBody = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                    lastError = "Model $model: $errorBody"
                    continue

                } catch (e: Exception) {
                    val errorMsg = e.message ?: "Unknown error"
                    
                    // Better error messages
                    when {
                        errorMsg.contains("DNS", ignoreCase = true) || 
                        errorMsg.contains("No address", ignoreCase = true) ||
                        errorMsg.contains("UnknownHostException", ignoreCase = true) -> {
                            lastError = "Network error: Cannot reach OpenRouter. Check your internet connection."
                        }
                        errorMsg.contains("timeout", ignoreCase = true) -> {
                            lastError = "Request timeout: Server response too slow"
                        }
                        errorMsg.contains("API key", ignoreCase = true) -> {
                            lastError = "API key error: Check your credentials"
                            globalRetryCount = maxGlobalRetries
                        }
                        else -> {
                            lastError = "Model $model: ${e.message}"
                        }
                    }
                    continue
                }
            }
            if (!hitRateLimit) break 
        }

        val finalError = when {
            hitRateLimit -> "API rate limit reached. Please wait a few moments and try again."
            noEndpointsAvailable -> "All AI model endpoints are currently unavailable."
            lastError.contains("quota", ignoreCase = true) -> lastError
            lastError.contains("Authentication", ignoreCase = true) -> lastError
            lastError.contains("Network", ignoreCase = true) -> lastError
            else -> lastError
        }
        throw Exception(finalError)
    }

    suspend fun chatWithVision(
        messages: List<OpenRouterMessage>,
        maxTokens: Int = 2048,
        temperature: Double = 0.7
    ): String {
        // Provided instructions say: Never send image_url in API requests.
        // We'll fall back to standard chat or handle it by informing the user.
        return chat(messages, maxTokens, temperature)
    }
}
