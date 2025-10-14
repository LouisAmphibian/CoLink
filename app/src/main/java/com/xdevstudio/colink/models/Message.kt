package com.xdevstudio.colink.models


import java.util.Date

data class Message(
    var id: String = "",
    var groupId: String = "",
    var senderId: String = "",
    var senderName: String = "",
    var content: String = "",
    var timestamp: Date = Date(),
    var type: MessageType = MessageType.TEXT,
    var isRead: Boolean = false,
    var imageUrl: String? = null
) {
    enum class MessageType {
        TEXT,
        IMAGE,
        SYSTEM,
        PAYMENT,
        LOCATION
    }

    fun getFormattedTime(): String {
        val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        return format.format(timestamp)
    }
}