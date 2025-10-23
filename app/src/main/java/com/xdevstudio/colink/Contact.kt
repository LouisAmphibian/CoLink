package com.xdevstudio.colink

data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val normalized: String,
    var isOnCoLink: Boolean = false,
    var userId: String? = null,
    var isSelected: Boolean = false
)
