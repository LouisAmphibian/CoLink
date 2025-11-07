package com.xdevstudio.colink.models


import java.util.Date

data class Transaction(
    var id: String = "",
    var groupId: String = "",
    var userId: String = "",
    var userName: String = "",
    var amount: Double = 0.0,
    var type: TransactionType = TransactionType.CONTRIBUTION,
    var status: TransactionStatus = TransactionStatus.PENDING,
    var description: String = "",
    var timestamp: Date = Date(),
    var approvedBy: MutableList<String> = mutableListOf(),
    var requiredApprovals: Int = 1
) {
    enum class TransactionType {
        CONTRIBUTION,
        WITHDRAWAL,
        EXPENSE
    }

    enum class TransactionStatus {
        PENDING,
        APPROVED,
        REJECTED,
        COMPLETED
    }

    fun getFormattedAmount(): String {
        return "R %.2f".format(amount)
    }

    fun needsMoreApprovals(): Boolean {
        return approvedBy.size < requiredApprovals
    }

    fun getApprovalProgress(): String {
        return "${approvedBy.size}/$requiredApprovals approvals"
    }
}