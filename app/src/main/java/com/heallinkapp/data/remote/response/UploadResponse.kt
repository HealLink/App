package com.heallinkapp.data.remote.response

import com.google.gson.annotations.SerializedName

data class FileUploadResponse(

    @field:SerializedName("status")
    val status: String? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("data")
    val data: FileUploadData? = null
)

data class FileUploadData(

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("result")
    val result: List<Float>? = null,

    @field:SerializedName("story")
    val story: String? = null,

    @field:SerializedName("createdAt")
    val createdAt: String? = null
)


data class UploadRequest(
    val token: String? = null,
    val title: String? = null,
    val sentence: String? = null,
    val date: String? = null
)