package com.rega.heallink.ui.auth

import androidx.lifecycle.ViewModel
import com.rega.heallink.data.UserRepository
import com.rega.heallink.data.remote.response.RegisterResponse


class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {

    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return userRepository.register(name, email, password)
    }
}
