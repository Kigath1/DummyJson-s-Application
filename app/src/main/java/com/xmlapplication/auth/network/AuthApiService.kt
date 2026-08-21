package com.xmlapplication.auth.network

import com.xmlapplication.auth.models.RefreshTokenRequest
import com.xmlapplication.auth.models.LoginRequest
import com.xmlapplication.auth.models.LoginResponse
import com.xmlapplication.auth.models.RefreshTokenResponse
import com.xmlapplication.auth.models.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @GET("auth/me")
    suspend fun getCurrentUser(
        @Header("authorization") token: String
    ): User


    @POST("auth/refresh")
    suspend fun tokenRefresh(
        @Body request: RefreshTokenRequest
    ): RefreshTokenResponse
}