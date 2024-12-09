package com.heallinkapp.data

import com.heallinkapp.data.local.UserPreferences
import com.heallinkapp.data.remote.response.LoginRequest
import com.heallinkapp.data.remote.response.LoginResponse
import com.heallinkapp.data.remote.response.RegisterResponse
import com.heallinkapp.data.remote.retrofit.ApiService
import kotlinx.coroutines.flow.Flow

class UserRepository private constructor(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences,
) {

    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        val response = apiService.register(name, email, password)

        if (response.error == true) {
            throw Exception(response.message ?: "Registration failed")
        }
        return response
    }

    suspend fun login(email: String, password: String): LoginResponse {
        val loginRequest = LoginRequest(email, password)
        val response = apiService.login(loginRequest)

        if (response.error == false) {
            val token = response.loginResult?.token
            val userName = response.loginResult?.name

            if (!token.isNullOrEmpty() && !userName.isNullOrEmpty()) {
                userPreferences.saveToken(
                    token = token,
                    userName = userName
                )
            } else {
                throw Exception("Invalid login result data")
            }
        } else {
            throw Exception(response.message ?: "Login failed")
        }

        return response
    }

    val userToken: Flow<String?> = userPreferences.userToken


    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(
            apiService: ApiService,
            userPreferences: UserPreferences
        ): UserRepository {
            return instance ?: synchronized(this) {
                instance ?: UserRepository(apiService, userPreferences).also {
                    instance = it
                }
            }
        }
    }
}
