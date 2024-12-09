package com.heallinkapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.heallinkapp.AuthViewModelFactory
import com.heallinkapp.R
import com.heallinkapp.data.remote.response.RegisterResponse
import com.heallinkapp.databinding.ActivityRegisterBinding
import com.heallinkapp.di.Injection
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val registerViewModel: RegisterViewModel by viewModels {
        AuthViewModelFactory(Injection.provideUserRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.tvLoginClick.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.signupButton.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                showToast("All fields are required")
            } else {
                registerUser(name, email, password)
            }
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = registerViewModel.register(name, email, password)
                if (response.error == false) {
                    showToast("Registration successful")
                    finish()
                } else {
                    showToast(response.message ?: "Registration failed")
                }
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, RegisterResponse::class.java)
                val errorMessage = errorBody?.message ?: "Registration failed"
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
}
