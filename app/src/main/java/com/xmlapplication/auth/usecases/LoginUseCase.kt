package com.xmlapplication.auth.usecases

import com.xmlapplication.auth.models.User
import com.xmlapplication.auth.repository.AuthRepository

class LoginUseCase (private val authrepository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String): Result<User>{
        return authrepository.login(username, password)
    }
}