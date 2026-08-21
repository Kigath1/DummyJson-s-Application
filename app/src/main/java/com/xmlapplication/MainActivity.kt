package com.xmlapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.xmlapplication.auth.repository.AuthRepository
import com.xmlapplication.auth.repository.AuthRepositoryImpl
import com.xmlapplication.auth.viewmodel.AuthUIState
import com.xmlapplication.auth.viewmodel.AuthViewModel
import com.xmlapplication.core.di.AppContainer
import com.xmlapplication.core.local.TokenManager
import com.xmlapplication.core.network.RetrofitInstance
import com.xmlapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: AuthViewModel by lazy {
        AppContainer.provideAuthViewModel(this, applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
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
                            viewModel.resetState() // avoid re-showing stale error on recomposition
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        if (isLoading) binding.tvError.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun navigateToHome() {
//        startActivity(Intent(this, HomeActivity::class.java))
//        finish()
        Toast.makeText(this, "Nigga Successfully Logged in ", Toast.LENGTH_SHORT).show()
    }
}