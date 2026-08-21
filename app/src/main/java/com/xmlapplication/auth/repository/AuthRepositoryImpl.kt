package com.xmlapplication.auth.repository

import coil.network.HttpException
import com.xmlapplication.core.local.TokenManager
import com.xmlapplication.auth.models.LoginRequest
import com.xmlapplication.auth.models.User
import com.xmlapplication.auth.network.AuthApiService
import okio.IOException

class AuthRepositoryImpl(
    private val api: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val response = api.login(LoginRequest(username = username, password = password))
            tokenManager.saveTokens(response.accessToken, response.refreshToken)

            Result.success(
                User(
                    id = response.id,
                    username = response.username,
                    email = response.email,
                    firstName = response.firstName,
                    lastName = response.lastName,
                    gender = response.gender,
                    image = response.image
                )
            )
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return try {
            Result.success(api.getCurrentUser(token = tokenManager.getAccessToken().toString()))
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    override fun isLoggedIn(): Boolean = tokenManager.getAccessToken() != null

    override fun logout() = tokenManager.clearTokens()
}