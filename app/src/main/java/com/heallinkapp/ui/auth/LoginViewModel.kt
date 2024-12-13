package com.heallinkapp.ui.auth

import androidx.lifecycle.ViewModel
import com.heallinkapp.data.UserRepository
import com.heallinkapp.data.remote.response.LoginResponse
class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {
    suspend fun login(email: String, password: String): LoginResponse {
        return userRepository.login(email, password)
    }
}