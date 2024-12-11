package com.heallinkapp.data.remote.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(

    @field:SerializedName("status")
    val status: String? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("data")
    val data: LoginData? = null
)

data class LoginData(
    @field:SerializedName("token")
    val token: String? = null,
    val name: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)
