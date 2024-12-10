package com.heallinkapp.ui.music

import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoApiService {
    @GET("tracks/")
    suspend fun getRelaxationMusic(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("tags") tags: String = "relax,meditation",
        @Query("limit") limit: Int = 30,
        @Query("include") include: String = "musicinfo"
    ): JamendoResponse
}


