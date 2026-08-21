package com.xmlapplication.auth.repository

import com.xmlapplication.auth.models.User

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun getCurrentUser(): Result<User>
    fun isLoggedIn(): Boolean
    fun logout()
}