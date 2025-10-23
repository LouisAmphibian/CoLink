package com.xdevstudio.colink

import com.google.firebase.Timestamp

data class User(
    val userId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)