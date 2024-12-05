package com.rega.heallink.ui.auth

import androidx.lifecycle.ViewModel
import com.rega.heallink.data.UserRepository

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    suspend fun login(email: String, password: String) = userRepository.login(email, password)
}