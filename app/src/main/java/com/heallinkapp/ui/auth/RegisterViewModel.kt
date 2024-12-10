package com.heallinkapp.ui.auth

import androidx.lifecycle.ViewModel
import com.heallinkapp.data.UserRepository
import com.heallinkapp.data.remote.response.RegisterResponse


class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {

    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return userRepository.register(name, email, password)
    }
}
