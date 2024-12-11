package com.heallinkapp.data

import android.util.Log
import com.heallinkapp.data.local.UserPreferences
import com.heallinkapp.data.remote.response.LoginRequest
import com.heallinkapp.data.remote.response.LoginResponse
import com.heallinkapp.data.remote.response.RegisterRequest
import com.heallinkapp.data.remote.response.RegisterResponse
import com.heallinkapp.data.remote.retrofit.ApiService
import kotlinx.coroutines.flow.Flow

class UserRepository private constructor(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences,
) {

    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        val registerRequest = RegisterRequest(name, email, password)
        val response = apiService.register(registerRequest)

        if (response.status != "success") {
            throw Exception(response.message ?: "Registration failed")
        }

        return response
    }

    suspend fun login(email: String, password: String): LoginResponse {
        val loginRequest = LoginRequest(email, password)
        val response = apiService.login(loginRequest)

        Log.d("LoginResponse", "Login response: ${response.status}, message: ${response.message}")

        if (response.status == "success") {
            val token = response.data?.token
            val userName = response.data?.name

            if (!token.isNullOrEmpty()&& !userName.isNullOrEmpty())  {
                userPreferences.saveToken(token,userName)
            } else {
                throw Exception("Login result does not contain a token")
            }
        } else {
            throw Exception(response.message ?: "Login failed")
        }

        return response
    }


    val userToken: Flow<String?> = userPreferences.userToken
    val userName: Flow<String?> = userPreferences.userName

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

