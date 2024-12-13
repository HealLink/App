package com.heallinkapp.data.remote.response

import com.google.gson.annotations.SerializedName

data class NotesResponse(

    @field:SerializedName("data")
    val data: List<DataItem?>? = null,

    @field:SerializedName("status")
    val status: String? = null
)

data class DataItem(

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("history")
    val history: History? = null
)

data class History(

    @field:SerializedName("result")
    val result: List<Float>? = null,

    @field:SerializedName("createdAt")
    val createdAt: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("token")
    val token: String? = null,

    @field:SerializedName("date")
    val date: String? = null,

    @field:SerializedName("story")
    val story: String? = null
)