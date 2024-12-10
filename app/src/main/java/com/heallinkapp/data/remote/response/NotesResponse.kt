package com.heallinkapp.data.remote.response

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class StoryResponse(

    @field:SerializedName("listNotes")
    val listStory: List<ListNoteItem?>? = emptyList(),

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null
)

@Parcelize
data class ListNoteItem(

    @field:SerializedName("id")
    var id: Int = 0,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("date")
    val date: String? = null,

    @field:SerializedName("score")
    var score : Double? = null,

):Parcelable
