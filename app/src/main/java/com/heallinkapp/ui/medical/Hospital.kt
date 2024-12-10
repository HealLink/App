package com.heallinkapp.ui.medical

import com.heallinkapp.R

data class Hospital(
    val name: String,
    val distance: Int,
    val latitude: Double,
    val longitude: Double,
    val image: Int = R.drawable.rsmoewardi
)