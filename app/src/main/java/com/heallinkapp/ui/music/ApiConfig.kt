package com.heallinkapp.ui.music


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    private const val JAMENDO_BASE_URL = "https://api.jamendo.com/v3.0/"
    private const val CLIENT_ID = "eeab465c"

    fun getClientId(): String = CLIENT_ID


    fun getJamendoApi(): JamendoApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(JAMENDO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(JamendoApiService::class.java)
    }
}

