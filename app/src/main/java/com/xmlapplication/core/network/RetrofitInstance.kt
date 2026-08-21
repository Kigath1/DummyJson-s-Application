package com.xmlapplication.core.network

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.xmlapplication.core.local.TokenManager
import com.xmlapplication.auth.network.AuthApiService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "https://dummyjson.com/"

    // Nullable instead of lateinit to avoid init-order crashes.
    private var tokenManager: TokenManager? = null

    fun initialize(context: Context) {
        tokenManager = TokenManager(context.applicationContext)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val converterFactory = json.asConverterFactory("application/json".toMediaType())

    // Built once. Shared Dispatcher + ConnectionPool for every client derived from it.
    private val baseClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val original = chain.request()

                val isAuthFreeEndpoint =
                    original.url.encodedPath.contains("auth/login") ||
                            original.url.encodedPath.contains("auth/refresh")

                val request = if (!isAuthFreeEndpoint) {
                    val token = tokenManager?.getAccessToken()
                    if (token != null) {
                        original.newBuilder()
                            .header("Authorization", "Bearer $token")
                            .build()
                    } else {
                        original
                    }
                } else {
                    original
                }

                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // No authenticator attached — used only to build the refresh call inside
    // TokenAuthenticator, so a failed refresh can never trigger itself again.
    private fun buildBaseRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(baseClient)
            .addConverterFactory(converterFactory)
            .build()
    }

    private fun buildAppRetrofit(): Retrofit {
        val tokenManagerNonNull = tokenManager
            ?: error("TokenManager not initialized. Call RetrofitInstance.initialize() first.")

        val baseRetrofit = buildBaseRetrofit()

        val authenticatedClient = baseClient.newBuilder()
            .authenticator(TokenAuthenticator(tokenManagerNonNull, baseRetrofit))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(authenticatedClient)
            .addConverterFactory(converterFactory)
            .build()
    }

    val apiService: AuthApiService by lazy {
        buildAppRetrofit().create(AuthApiService::class.java)
    }
}