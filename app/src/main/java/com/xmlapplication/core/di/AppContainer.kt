package com.xmlapplication.core.di


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.xmlapplication.auth.repository.AuthRepository
import com.xmlapplication.auth.repository.AuthRepositoryImpl
import com.xmlapplication.auth.usecases.GetCurrentUserUseCase
import com.xmlapplication.auth.usecases.IsLoggedInUseCase
import com.xmlapplication.auth.usecases.LoginUseCase
import com.xmlapplication.auth.usecases.LogoutUseCase
import com.xmlapplication.auth.viewmodel.AuthViewModel
import com.xmlapplication.core.local.TokenManager
import com.xmlapplication.core.network.RetrofitInstance

/**
 * Simple manual DI container. Everything is built lazily and cached as a
 * singleton for the process lifetime — one TokenManager, one Retrofit,
 * one AuthRepository. Swap this out for Hilt later without touching
 * the Activities, since they only ever call provideAuthViewModel().
 */
object AppContainer {

    @Volatile
    private var tokenManager: TokenManager? = null

    @Volatile
    private var authRepository: AuthRepository? = null

    private fun getTokenManager(context: Context): TokenManager {
        return tokenManager ?: synchronized(this) {
            tokenManager ?: TokenManager(context.applicationContext).also {
                tokenManager = it
            }
        }
    }

    private fun getAuthRepository(context: Context): AuthRepository {
        return authRepository ?: synchronized(this) {
            authRepository ?: run {
                // RetrofitInstance needs TokenManager wired in before apiService is touched.
                RetrofitInstance.initialize(context.applicationContext)
                AuthRepositoryImpl(
                    api = RetrofitInstance.apiService,
                    tokenManager = getTokenManager(context)
                ).also { authRepository = it }
            }
        }
    }

    fun provideAuthViewModel(owner: androidx.lifecycle.ViewModelStoreOwner, context: Context): AuthViewModel {
        val repository = getAuthRepository(context)

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(
                    loginUseCase = LoginUseCase(repository),
                    getCurrentUserUseCase = GetCurrentUserUseCase(repository),
                    logoutUseCase = LogoutUseCase(repository),
                    isLoggedInUseCase = IsLoggedInUseCase(repository)
                ) as T
            }
        }

        return ViewModelProvider(owner, factory)[AuthViewModel::class.java]
    }
}