package com.xdevstudio.colink.models


import java.util.Date

data class EventGroup(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var eventDate: Date? = null,
    var eventTime: String = "",
    var location: String = "",
    var budget: Double = 0.0,
    var currentFunds: Double = 0.0,
    var lastMessage: String = "",
    var lastMessageTime: Date = Date(),
    var unreadCount: Int = 0,
    var memberIds: MutableList<String> = mutableListOf(),
    var creatorId: String = "",
    var createdAt: Date = Date(),
    var hasFundingActive: Boolean = false
) {
    fun getFormattedEventDate(): String {
        return eventDate?.let {
            val format = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            format.format(it)
        } ?: ""
    }

    fun getFormattedEventDateTime(): String {
        return "${getFormattedEventDate()}, $eventTime"
    }

    fun getFundingProgress(): Int {
        return if (budget > 0) {
            ((currentFunds / budget) * 100).toInt()
        } else 0
    }

    fun getMemberCount(): Int = memberIds.size
}