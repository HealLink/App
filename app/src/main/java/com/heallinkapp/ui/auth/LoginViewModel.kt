package com.heallinkapp.ui.auth

import androidx.lifecycle.ViewModel
import com.heallinkapp.data.UserRepository

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    suspend fun login(email: String, password: String) = userRepository.login(email, password)
}