package com.heallinkapp.data.remote.retrofit

import com.heallinkapp.data.remote.response.FileUploadResponse
import com.heallinkapp.data.remote.response.LoginRequest
import com.heallinkapp.data.remote.response.LoginResponse
import com.heallinkapp.data.remote.response.NotesResponse
import com.heallinkapp.data.remote.response.RegisterRequest
import com.heallinkapp.data.remote.response.RegisterResponse
import com.heallinkapp.data.remote.response.UploadRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("register")
    @Headers("Content-Type: application/json")
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): RegisterResponse

    @POST("login")
    @Headers("Content-Type: application/json")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): LoginResponse

    @GET("predict/histories")
    suspend fun getStories(): NotesResponse

    @POST("predict")
    @Headers("Content-Type: application/json")
    suspend fun addStory(
        @Body uploadRequest: UploadRequest
    ): FileUploadResponse
}