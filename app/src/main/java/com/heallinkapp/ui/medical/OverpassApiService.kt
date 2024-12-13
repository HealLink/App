package com.heallinkapp.ui.medical


import retrofit2.http.GET
import retrofit2.http.Query

interface OverpassApiService {
    @GET("interpreter")
    suspend fun searchNearbyHospitals(
        @Query("data") query: String
    ): OverpassResponse
}