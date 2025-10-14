package com.xdevstudio.colink.models

import java.util.Date

data class User(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var profileImageUrl: String = "",
    var bio: String = "",
    var rewardPoints: Int = 0,
    var createdAt: Date = Date()
) {
    fun getInitial(): String {
        return if (name.isNotEmpty()) {
            name.first().toString().uppercase()
        } else "?"
    }
}