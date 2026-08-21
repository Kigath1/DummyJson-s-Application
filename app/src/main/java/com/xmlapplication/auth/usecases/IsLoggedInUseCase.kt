package com.xmlapplication.auth.usecases

import com.xmlapplication.auth.repository.AuthRepository

class IsLoggedInUseCase(private val authRepository: AuthRepository) {
    operator fun invoke() : Boolean = authRepository.isLoggedIn()
}