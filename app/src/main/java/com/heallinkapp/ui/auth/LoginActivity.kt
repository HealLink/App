package com.heallinkapp.ui.auth

import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
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
import com.heallinkapp.data.remote.response.LoginResponse
import com.heallinkapp.databinding.ActivityLoginBinding
import com.heallinkapp.di.Injection
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

        binding.tvRegisterClick.setOnClickListener {
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
                if (response.error == false) {
                    showToast("Login successful: Welcome ${response.loginResult?.name}")
                    navigateToMainScreen()
                } else {
                    showToast(response.message ?: "Login failed")
                }
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, LoginResponse::class.java)
                val errorMessage = errorBody?.message ?: "Login failed"
                showToast(errorMessage)
            } catch (e: IOException) {
                showToast("Network error: Please check your connection")
            } catch (e: Exception) {
                showToast("An unexpected error occurred")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }



    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainScreen() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
