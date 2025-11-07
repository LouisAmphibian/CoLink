package com.xdevstudio.colink

import java.util.*

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val date: Date = Date(),
    val time: String = "",
    val location: String = "",
    val budget: Double = 0.0,
    val createdBy: String = "",
    val status: String = "pending", // pending, active, cancelled
    val members: MutableList<GroupMember> = mutableListOf(),
    val invitedMembers: MutableList<InvitedMember> = mutableListOf(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val canChat : Boolean = false
)

data class GroupMember(
    val userId: String,
    val role: String = "member", // admin, member
    val joinedAt: Date = Date(),
    val accepted: Boolean = false
)

data class InvitedMember(
    val phoneNumber: String,
    val name: String,
    val isOnCoLink: Boolean = false,
    val userId: String? = null,
    val invitedAt: Date = Date(),
    val invitedVia: String = "whatsapp", // whatsapp, colink
    var status: String = "pending"
)