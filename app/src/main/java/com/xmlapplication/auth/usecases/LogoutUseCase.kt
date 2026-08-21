package com.xmlapplication.auth.usecases

import com.xmlapplication.auth.repository.AuthRepository

class LogoutUseCase(private val authRepository: AuthRepository) {
    operator fun invoke() = authRepository.logout()
}