package com.heallinkapp.data.remote.response

import com.google.gson.annotations.SerializedName

data class RegisterResponse(

    @field:SerializedName("status")
    val status: String? = null,

    @field:SerializedName("message")
    val message: String? = null
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)