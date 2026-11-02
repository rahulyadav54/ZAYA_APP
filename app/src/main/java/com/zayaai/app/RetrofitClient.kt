package com.zayaai.app

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class ChatRequest(
    val message: String
)

data class ChatResponse(
    val response: String? = null,
    val error: String? = null
)

// ─── PPT Generation Models ───
data class PPTCreateRequest(
    val topic: String,
    val slides: List<PPTSlide>
)

data class PPTSlide(
    val title: String,
    val bullets: List<String>
)

data class PPTCreateResponse(
    val fileUrl: String? = null,
    val fileName: String? = null,
    val error: String? = null
)

interface BackendApi {
    @POST("api/chat")
    suspend fun chat(
        @Header("Authorization") token: String,
        @Body request: ChatRequest
    ): ChatResponse

    @POST("api/ppt-create")
    suspend fun createPPT(
        @Body request: PPTCreateRequest
    ): PPTCreateResponse
}

object RetrofitClient {
    private const val GROQ_BASE_URL = "https://api.groq.com/openai/"
    private val BACKEND_BASE_URL: String
        get() = if (BuildConfig.BACKEND_URL.endsWith("/")) BuildConfig.BACKEND_URL else "${BuildConfig.BACKEND_URL}/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val groqApi: GroqApi by lazy {
        Retrofit.Builder()
            .baseUrl(GROQ_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(GroqApi::class.java)
    }

    val backendApi: BackendApi by lazy {
        Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(BackendApi::class.java)
    }
}
