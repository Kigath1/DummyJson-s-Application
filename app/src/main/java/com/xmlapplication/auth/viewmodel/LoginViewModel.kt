package com.xmlapplication.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xmlapplication.auth.models.User
import com.xmlapplication.auth.usecases.GetCurrentUserUseCase
import com.xmlapplication.auth.usecases.IsLoggedInUseCase
import com.xmlapplication.auth.usecases.LoginUseCase
import com.xmlapplication.auth.usecases.LogoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class AuthUIState {
    data object Idle : AuthUIState()
    data object Loading : AuthUIState()
    data class Success(val user: User) : AuthUIState()
    data class Error(val message: String) : AuthUIState()
}

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUIState>(AuthUIState.Idle)
    val uiState: StateFlow<AuthUIState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUIState.Loading

            loginUseCase(username, password)
                .onSuccess { user ->
                    _uiState.value = AuthUIState.Success(user)
                }
                .onFailure { error ->
                    _uiState.value = AuthUIState.Error(mapError(error))
                }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.value = AuthUIState.Loading

            getCurrentUserUseCase()
                .onSuccess { user -> _uiState.value = AuthUIState.Success(user) }
                .onFailure { error -> _uiState.value = AuthUIState.Error(mapError(error)) }
        }
    }

    fun logout() {
        logoutUseCase()
        _uiState.value = AuthUIState.Idle
    }

    fun isLoggedIn(): Boolean = isLoggedInUseCase()

    fun resetState() {
        _uiState.value = AuthUIState.Idle
    }

    private fun mapError(error: Throwable): String {
        return when (error) {
            is HttpException -> when (error.code()) {
                400 -> "Invalid username or password"
                401 -> "Session expired, please log in again"
                else -> "Server error (${error.code()})"
            }
            is IOException -> "Network error — check your connection"
            else -> error.message ?: "Something went wrong"
        }
    }
}