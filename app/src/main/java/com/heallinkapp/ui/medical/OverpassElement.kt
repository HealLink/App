package com.heallinkapp.ui.medical

data class OverpassElement(
    val id: Long,
    val lat: Double,
    val lon: Double,
    val tags: Map<String, String>? = null
)