package com.xmlapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.xmlapplication.auth.viewmodel.AuthUIState
import com.xmlapplication.auth.viewmodel.AuthViewModel
import com.xmlapplication.core.di.AppContainer
import com.xmlapplication.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // Manual DI for now — swap this line out later if you move to Hilt.
    private val viewModel: AuthViewModel by lazy {
        AppContainer.provideAuthViewModel(this, applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Already logged in from a previous session — skip straight to Home.
        if (viewModel.isLoggedIn()) {
            navigateToHome()
            return
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword.text?.toString()?.trim().orEmpty()

            if (username.isEmpty() || password.isEmpty()) {
                showError("Username and password are required")
                return@setOnClickListener
            }

            viewModel.login(username, password)
        }

        observeUiState()
    }

    private fun observeUiState() {
        // repeatOnLifecycle(STARTED) pauses collection when the Activity isn't
        // visible (e.g. backgrounded) and resumes it automatically — avoids
        // updating views while the Activity is stopped.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AuthUIState.Idle -> {
                            setLoading(false)
                        }
                        is AuthUIState.Loading -> {
                            setLoading(true)
                        }
                        is AuthUIState.Success -> {
                            setLoading(false)
                            navigateToHome()
                        }
                        is AuthUIState.Error -> {
                            setLoading(false)
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnLogin.isEnabled = !isLoading
        if (isLoading) binding.tvError.visibility = android.view.View.GONE
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = android.view.View.VISIBLE
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish() // remove LoginActivity from back stack so back button doesn't return to it
    }
}