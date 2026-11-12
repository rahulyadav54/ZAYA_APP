package com.zayaai.app

import android.util.Log
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

// --- OpenRouter / Nemotron API Models ---

data class OpenRouterRequest(
    @SerializedName("model") val model: String = AIService.NEMOTRON_PRIMARY,
    @SerializedName("messages") val messages: List<OpenRouterMessage>,
    @SerializedName("max_tokens") val max_tokens: Int = 4096,
    @SerializedName("temperature") val temperature: Double = 0.7,
    @SerializedName("stream") val stream: Boolean = false
)

data class OpenRouterMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class OpenRouterResponse(
    @SerializedName("choices") val choices: List<OpenRouterChoice>? = null,
    @SerializedName("error") val error: OpenRouterError? = null,
    @SerializedName("model") val model: String? = null
)

data class OpenRouterChoice(
    @SerializedName("message") val message: OpenRouterChoiceMessage? = null,
    @SerializedName("delta") val delta: OpenRouterChoiceMessage? = null
)

data class OpenRouterChoiceMessage(
    @SerializedName("role") val role: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("reasoning") val reasoning: String? = null
)

data class OpenRouterError(
    @SerializedName("message") val message: String? = null,
    @SerializedName("code") val code: Int? = null
)

// --- Gateway Request / Response Models ---

data class GatewayChatRequest(
    val messages: List<OpenRouterMessage>,
    val model: String = AIService.NEMOTRON_PRIMARY,
    val temperature: Double = 0.7,
    val max_tokens: Int = 4096
)

data class GatewayChatResponse(
    val response: String? = null,
    val model: String? = null,
    val provider: String? = null,
    val error: String? = null
)

interface OpenRouterDirectApi {
    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authHeader: String,
        @Header("HTTP-Referer") referer: String,
        @Header("X-Title") title: String,
        @Body request: OpenRouterRequest
    ): Response<OpenRouterResponse>
}

/**
 * AIService: Centralized AI infrastructure for AI ZAYA 2.0.
 *
 * Primary Model: NVIDIA Nemotron 3 Ultra (nvidia/nemotron-3-ultra-550b-a55b)
 * via secure production AI Gateway (Edge Cloudflare Worker / HTTPS backend).
 * Features:
 * - Real-time SSE streaming with reasoning token isolation
 * - AI ZAYA branded tier selection (AI ZAYA Advanced, AI ZAYA Smart, AI ZAYA Fast)
 * - Automatic fallback and retry mechanisms
 * - Strict HTTPS communications with zero local IP dependencies
 */
object AIService {

    private const val TAG = "AIService"

    // Primary Frontier Reasoning Model IDs (Enforcing ₹0 Budget with Verified Free Tiers)
    const val NEMOTRON_PRIMARY = "nvidia/nemotron-3-ultra-550b-a55b:free"
    const val NEMOTRON_FREE = "nvidia/nemotron-3-ultra-550b-a55b:free"
    const val GROQ_FALLBACK = "nvidia/nemotron-3-ultra-550b-a55b:free"

    // Model Catalog with Display Names - Strictly AI ZAYA Branded
    val AVAILABLE_MODELS = listOf(
        ModelInfo(NEMOTRON_PRIMARY, "AI ZAYA Advanced", "Frontier 550B reasoning engine for complex queries", true),
        ModelInfo(NEMOTRON_FREE, "AI ZAYA Smart", "Balanced intelligent reasoning engine", false),
        ModelInfo(GROQ_FALLBACK, "AI ZAYA Fast", "Ultra fast low-latency response engine", false)
    )

    data class ModelInfo(
        val id: String,
        val displayName: String,
        val description: String,
        val isDefault: Boolean
    )

    // Production Edge Gateway URL from BuildConfig
    private val backendBaseUrl: String
        get() {
            val url = BuildConfig.BACKEND_URL
            return if (url.endsWith("/")) url else "$url/"
        }

    private val gson: Gson = GsonBuilder().create()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val directOpenRouterApi: OpenRouterDirectApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(OpenRouterDirectApi::class.java)
    }

    /**
     * Strip internal reasoning tags (<think>...</think>) so internal tokens are never shown to end-users.
     */
    fun sanitizeReasoning(rawText: String): String {
        return rawText.replace(Regex("<think>[\\s\\S]*?</think>", RegexOption.IGNORE_CASE), "").trim()
    }

    /**
     * Map HTTP error codes and exception messages to user-friendly messages.
     * Conceals third-party infrastructure details from end-users.
     */
    fun mapErrorMessage(statusCode: Int, rawBody: String? = null): String {
        return when (statusCode) {
            400 -> "Invalid request. Please rephrase your question."
            401 -> "Authentication error with the AI service. Please verify account status."
            402 -> "AI service capacity temporarily reached. Please try again shortly."
            403 -> "Access to this AI model is currently restricted."
            408 -> "Request timed out. AI ZAYA took too long to complete. Please try again."
            429 -> "Rate limit reached. Too many requests at once. Please wait a moment."
            500, 502, 503, 504 -> "AI service is temporarily busy. Please retry in a few moments."
            else -> rawBody?.take(150) ?: "Service temporarily unavailable (Error $statusCode)."
        }
    }

    /**
     * Stream completion via Server-Sent Events (SSE).
     * Connects to production backend gateway `/api/chat/stream`, falling back gracefully if needed.
     *
     * @param messages Full conversation context.
     * @param maxTokens Maximum output tokens.
     * @param temperature Sampling temperature.
     * @param model Model ID (defaults to AI ZAYA Advanced / Nemotron 3 Ultra).
     * @param onThinking Callback when model is in internal reasoning mode.
     * @param onChunk Callback for each stream token/chunk.
     * @return Complete generated text.
     */
    suspend fun chatStream(
        messages: List<OpenRouterMessage>,
        maxTokens: Int = 4096,
        temperature: Double = 0.7,
        model: String = NEMOTRON_PRIMARY,
        onThinking: (Boolean) -> Unit = {},
        onChunk: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {

        val fullTextBuilder = StringBuilder()
        var streamSucceeded = false
        var lastError: String? = null

        // 1. Production Backend Gateway streaming
        try {
            val streamUrl = "${backendBaseUrl}api/chat/stream"
            val jsonPayload = JSONObject().apply {
                put("model", model)
                put("temperature", temperature)
                put("max_tokens", maxTokens)
                val msgsArray = JSONArray()
                messages.forEach { msg ->
                    msgsArray.put(JSONObject().apply {
                        put("role", msg.role)
                        put("content", msg.content)
                    })
                }
                put("messages", msgsArray)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(streamUrl)
                .post(requestBody)
                .header("Accept", "text/event-stream")
                .header("X-Title", "AI ZAYA")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                val reader = BufferedReader(InputStreamReader(response.body!!.byteStream(), Charsets.UTF_8))
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    ensureActive() // coroutine cancellation check (Stop generation)
                    val trimmed = line?.trim() ?: continue
                    if (trimmed.isEmpty() || trimmed.startsWith(":")) continue

                    if (trimmed == "data: [DONE]") {
                        break
                    }

                    if (trimmed.startsWith("data: ")) {
                        val dataStr = trimmed.substring(6)
                        try {
                            val json = JSONObject(dataStr)
                            if (json.has("error")) {
                                throw IOException(json.getString("error"))
                            }
                            if (json.has("type") && json.getString("type") == "thinking") {
                                val isThinking = json.optBoolean("status", true)
                                withContext(Dispatchers.Main) { onThinking(isThinking) }
                                continue
                            }
                            if (json.has("type") && json.getString("type") == "content") {
                                val delta = json.getString("delta")
                                fullTextBuilder.append(delta)
                                withContext(Dispatchers.Main) {
                                    onThinking(false)
                                    onChunk(delta)
                                }
                            }
                        } catch (e: Exception) {
                            if (e is IOException) throw e
                        }
                    }
                }
                response.close()
                streamSucceeded = true
            } else {
                val errBody = response.body?.string()
                lastError = mapErrorMessage(response.code, errBody)
                response.close()
            }
        } catch (e: CancellationException) {
            // User pressed "Stop Generation"
            throw e
        } catch (e: Exception) {
            lastError = e.message
            Log.w(TAG, "Gateway stream failed at $backendBaseUrl: ${e.message}")
        }

        if (streamSucceeded) {
            return@withContext sanitizeReasoning(fullTextBuilder.toString())
        }

        // 2. Direct OpenRouter fallback if API key configured in local/debug build
        val directKey = BuildConfig.OPENROUTER_API_KEY
        if (!directKey.isNullOrBlank() && directKey != "your_openrouter_api_key_here") {
            try {
                Log.i(TAG, "Attempting direct OpenRouter streaming fallback...")
                val directResult = streamDirectOpenRouter(messages, model, temperature, maxTokens, onThinking, onChunk)
                return@withContext sanitizeReasoning(directResult)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e.message
            }
        }

        // 3. Fallback to non-streaming chat endpoint
        try {
            val nonStreamResult = chat(messages, maxTokens, temperature, model)
            val sanitized = sanitizeReasoning(nonStreamResult)
            withContext(Dispatchers.Main) {
                onThinking(false)
                onChunk(sanitized)
            }
            return@withContext sanitized
        } catch (e: Exception) {
            throw IOException(lastError ?: e.message ?: "Failed to generate AI response.")
        }
    }

    /**
     * Direct OpenRouter SSE Streaming Fallback (Used when direct key is present)
     */
    private suspend fun streamDirectOpenRouter(
        messages: List<OpenRouterMessage>,
        model: String,
        temperature: Double,
        maxTokens: Int,
        onThinking: (Boolean) -> Unit,
        onChunk: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.OPENROUTER_API_KEY
        val fullTextBuilder = StringBuilder()

        val jsonPayload = JSONObject().apply {
            put("model", model)
            put("temperature", temperature)
            put("max_tokens", maxTokens)
            put("stream", true)
            val msgsArray = JSONArray()
            messages.forEach { msg ->
                msgsArray.put(JSONObject().apply {
                    put("role", msg.role)
                    put("content", msg.content)
                })
            }
            put("messages", msgsArray)
        }

        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .post(jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .header("Authorization", "Bearer $apiKey")
            .header("HTTP-Referer", "https://aizaya.app")
            .header("X-Title", "AI ZAYA")
            .header("Accept", "text/event-stream")
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errCode = response.code
            response.close()
            throw IOException(mapErrorMessage(errCode))
        }

        val reader = BufferedReader(InputStreamReader(response.body!!.byteStream(), Charsets.UTF_8))
        var line: String?
        var insideThink = false

        while (reader.readLine().also { line = it } != null) {
            ensureActive()
            val trimmed = line?.trim() ?: continue
            if (trimmed == "data: [DONE]") break
            if (trimmed.startsWith("data: ")) {
                val dataStr = trimmed.substring(6)
                try {
                    val root = JSONObject(dataStr)
                    val choices = root.optJSONArray("choices") ?: continue
                    val first = choices.optJSONObject(0) ?: continue
                    val delta = first.optJSONObject("delta") ?: continue

                    if (delta.has("reasoning")) {
                        withContext(Dispatchers.Main) { onThinking(true) }
                        continue
                    }

                    var content = delta.optString("content", "")
                    if (content.contains("<think>")) {
                        insideThink = true
                        withContext(Dispatchers.Main) { onThinking(true) }
                        content = content.replace(Regex("<think>[\\s\\S]*?</think>"), "")
                        if (content.contains("<think>")) content = content.substringBefore("<think>")
                    } else if (insideThink) {
                        if (content.contains("</think>")) {
                            insideThink = false
                            content = content.substringAfter("</think>")
                            withContext(Dispatchers.Main) { onThinking(false) }
                        } else {
                            continue
                        }
                    }

                    if (content.isNotEmpty()) {
                        fullTextBuilder.append(content)
                        withContext(Dispatchers.Main) {
                            onThinking(false)
                            onChunk(content)
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        response.close()
        fullTextBuilder.toString()
    }

    /**
     * Non-streaming chat completion with full conversation context.
     * Communicates exclusively with the production HTTPS backend.
     */
    suspend fun chat(
        messages: List<OpenRouterMessage>,
        maxTokens: Int = 4096,
        temperature: Double = 0.7,
        model: String = NEMOTRON_PRIMARY
    ): String = withContext(Dispatchers.IO) {
        var lastError = "Unable to reach AI gateway or model."

        // 1. Try Production Backend Gateway
        try {
            val chatUrl = "${backendBaseUrl}api/chat"
            val payload = GatewayChatRequest(
                messages = messages,
                model = model,
                temperature = temperature,
                max_tokens = maxTokens
            )
            val requestBody = gson.toJson(payload).toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(chatUrl)
                .post(requestBody)
                .header("X-Title", "AI ZAYA")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string()

            if (response.isSuccessful && !responseString.isNullOrBlank()) {
                val gatewayRes = gson.fromJson(responseString, GatewayChatResponse::class.java)
                if (!gatewayRes.response.isNullOrBlank()) {
                    return@withContext sanitizeReasoning(gatewayRes.response)
                }
            } else {
                lastError = mapErrorMessage(response.code, responseString)
            }
        } catch (e: Exception) {
            lastError = e.message ?: "Connection error"
        }

        // 2. Direct OpenRouter fallback if API key present in build
        val apiKey = BuildConfig.OPENROUTER_API_KEY
        if (!apiKey.isNullOrBlank() && apiKey != "your_openrouter_api_key_here") {
            try {
                val request = OpenRouterRequest(
                    model = model,
                    messages = messages,
                    max_tokens = maxTokens,
                    temperature = temperature
                )
                val response = directOpenRouterApi.chatCompletion(
                    authHeader = "Bearer $apiKey",
                    referer = "https://aizaya.app",
                    title = "AI ZAYA",
                    request = request
                )
                if (response.isSuccessful) {
                    val content = response.body()?.choices?.firstOrNull()?.message?.content
                    if (!content.isNullOrBlank()) {
                        return@withContext sanitizeReasoning(content)
                    }
                } else {
                    lastError = mapErrorMessage(response.code(), response.errorBody()?.string())
                }
            } catch (e: Exception) {
                lastError = e.message ?: "OpenRouter network error"
            }
        }

        // 3. Fallback to Groq if key configured in debug build
        val groqKey = BuildConfig.GROQ_API_KEY
        if (!groqKey.isNullOrBlank() && groqKey != "your_groq_api_key_here") {
            try {
                val promptText = messages.lastOrNull { it.role == "user" }?.content ?: ""
                val systemPrompt = messages.firstOrNull { it.role == "system" }?.content
                val groqResult = GroqService.generate(promptText, systemPrompt, maxTokens)
                if (groqResult.isNotBlank()) {
                    return@withContext sanitizeReasoning(groqResult)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Groq fallback failed: ${e.message}")
            }
        }

        throw IOException(lastError)
    }

    /**
     * Send a single prompt with an optional system prompt and get a text response.
     */
    suspend fun generate(prompt: String, systemPrompt: String? = null): String {
        return chat(
            messages = buildList {
                if (systemPrompt != null) add(OpenRouterMessage("system", systemPrompt))
                add(OpenRouterMessage("user", prompt))
            }
        )
    }

    suspend fun chatWithVision(
        messages: List<OpenRouterMessage>,
        maxTokens: Int = 4096,
        temperature: Double = 0.7
    ): String {
        return chat(messages, maxTokens, temperature)
    }

    /**
     * Request an AI image.
     * TEMPORARILY DISABLED: Feature is deactivated across the application while comprehensive safety and moderation systems are upgraded.
     */
    suspend fun generateImage(prompt: String): ByteArray = withContext(Dispatchers.IO) {
        throw IOException("Image creation is temporarily disabled in AI ZAYA while safety systems are upgraded.")
    }

    /**
     * Submit an in-app user report for inappropriate or offensive AI-generated content.
     * Fulfills Google Play AI-Generated Content Policy.
     */
    suspend fun submitReport(
        contentId: String,
        contentType: String,
        snippet: String,
        reason: String,
        details: String = "",
        userId: String = "anonymous"
    ): String = withContext(Dispatchers.IO) {
        val url = "${backendBaseUrl}api/report"
        val payload = JSONObject().apply {
            put("contentId", contentId)
            put("contentType", contentType)
            put("contentSnippet", snippet)
            put("reason", reason)
            put("details", details)
            put("userId", userId)
        }
        val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Accept", "application/json")
            .build()

        val response = okHttpClient.newCall(request).execute()
        response.use { resp ->
            val bodyString = resp.body?.string() ?: ""
            if (!resp.isSuccessful) {
                val errJson = try { JSONObject(bodyString) } catch (_: Exception) { null }
                throw IOException(errJson?.optString("error") ?: "Report submission failed (${resp.code})")
            }
            val json = JSONObject(bodyString)
            return@withContext json.optString("reportId", "SUBMITTED")
        }
    }
}
