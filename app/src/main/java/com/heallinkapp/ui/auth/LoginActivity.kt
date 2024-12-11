package com.heallinkapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.heallinkapp.AuthViewModelFactory
import com.heallinkapp.MainActivity
import com.heallinkapp.R
import com.heallinkapp.data.local.UserPreferences
import com.heallinkapp.data.remote.response.LoginResponse
import com.heallinkapp.databinding.ActivityLoginBinding
import com.heallinkapp.di.Injection
import com.heallinkapp.ui.OnBoardingActivity
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels {
        AuthViewModelFactory(Injection.provideUserRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.signinButton.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                showToast("Email and password cannot be empty")
            } else {
                loginUser(email, password)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = loginViewModel.login(email, password)
                if (response.data?.token != null) {
                    navigateToMainScreen()
                } else {
                    showToast(response.message ?: "Login failed")
                }
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, LoginResponse::class.java)
                val errorMessage = errorBody?.message ?: "Login failed"
                showToast(errorMessage)

                // Log the exception for debugging
                Log.e("LoginError", "HTTP Error: ${e.message()}", e)
            } catch (e: IOException) {
                // Network error
                showToast("Network error: Please check your connection")

                // Log the exception for debugging
                Log.e("LoginError", "IO Error: ${e.message}", e)
            } catch (e: Exception) {
                // Unexpected error
                showToast("An unexpected error occurred")

                // Log the exception for debugging
                Log.e("LoginError", "Unexpected Error: ${e.message}", e)
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainScreen() {
        val userRepository = Injection.provideUserRepository(this)

        lifecycleScope.launch {
            val userPreferences = UserPreferences.newInstance(this@LoginActivity)
            userPreferences.isFirstTimeFlow.collect { isFirstTime ->
                if (isFirstTime) {
                    startActivity(Intent(this@LoginActivity, OnBoardingActivity::class.java))
                } else {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                }
                finish()
            }
        }

    }
}