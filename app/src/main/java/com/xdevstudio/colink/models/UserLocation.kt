package com.xdevstudio.colink.models

data class UserLocation(
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0L,
    val movementMode: String = "Walking",
    val accuracy: Float = 0f
)