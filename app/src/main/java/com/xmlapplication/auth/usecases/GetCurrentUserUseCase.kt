package com.xmlapplication.auth.usecases

import com.xmlapplication.auth.models.User
import com.xmlapplication.auth.repository.AuthRepository

class GetCurrentUserUseCase(private val authRepository: AuthRepository){
    suspend operator fun invoke(): Result<User> = authRepository.getCurrentUser()
}