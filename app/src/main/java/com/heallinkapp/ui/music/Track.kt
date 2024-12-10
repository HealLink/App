package com.heallinkapp.ui.music

data class Track(
    val id: String,
    val name: String,
    val artist_name: String,
    val duration: Int,
    val audio: String,
    val image: String
)