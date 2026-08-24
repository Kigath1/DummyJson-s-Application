package com.xmlapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.xmlapplication.auth.LoginActivity
import com.xmlapplication.auth.viewmodel.AuthUIState
import com.xmlapplication.auth.viewmodel.AuthViewModel
import com.xmlapplication.core.di.AppContainer
import com.xmlapplication.databinding.ActivityHomeBinding
import kotlinx.coroutines.launch
import kotlin.jvm.java

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val viewModel: AuthViewModel by lazy {
        AppContainer.provideAuthViewModel(this, applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            navigateToLogin()
        }

        observeUiState()
        viewModel.loadCurrentUser() // hits /auth/me using the stored token
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AuthUIState.Success -> {
                            binding.tvWelcome.text = "Welcome, ${state.user.firstName}"
                            Glide.with(this@HomeActivity)
                                .load(state.user.image)
                                .circleCrop()
                                .into(binding.ivAvatar)
                        }
                        is AuthUIState.Error -> {
                            // Token likely invalid/expired beyond refresh — bounce to login.
                            navigateToLogin()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}