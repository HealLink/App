package com.rega.heallink

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rega.heallink.data.UserRepository
import com.rega.heallink.ui.auth.LoginViewModel
import com.rega.heallink.ui.auth.RegisterViewModel


class AuthViewModelFactory(private val repository: UserRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}