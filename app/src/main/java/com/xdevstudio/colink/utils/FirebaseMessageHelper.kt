
package com.xdevstudio.colink.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.xdevstudio.colink.models.Message
import kotlinx.coroutines.tasks.await

object FirebaseMessageHelper {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Get current user ID
    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "anonymous_user"
    }

    // Get current user name
    suspend fun getCurrentUserName(): String {
        val userId = getCurrentUserId()
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.getString("name") ?: "User"
        } catch (e: Exception) {
            "User"
        }
    }

    // ==================== GROUP MESSAGES ====================

    /**
     * Send message to group chat
     */
    suspend fun sendGroupMessage(message: Message): Result<String> {
        return try {
            val messageRef = db.collection("messages").document()
            message.id = messageRef.id

            messageRef.set(message).await()

            // Update group's last message
            updateGroupLastMessage(message.groupId, message.content, message.timestamp)

            Result.success(message.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Listen to group messages in real-time
     */
    fun listenToGroupMessages(
        groupId: String,
        onMessagesChanged: (List<Message>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("messages")
            .whereEqualTo("groupId", groupId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val messages = snapshots?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Message::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                onMessagesChanged(messages)
            }
    }

    /**
     * Mark messages as read
     */
    suspend fun markMessagesAsRead(groupId: String, userId: String) {
        try {
            val messages = db.collection("messages")
                .whereEqualTo("groupId", groupId)
                .get()
                .await()

            messages.documents.forEach { doc ->
                val senderId = doc.getString("senderId") ?: ""
                val isRead = doc.getBoolean("isRead") ?: false

                if (senderId != userId && !isRead) {
                    doc.reference.update("isRead", true).await()
                }
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    /**
     * Update group's last message
     */
    private suspend fun updateGroupLastMessage(
        groupId: String,
        lastMessage: String,
        timestamp: java.util.Date
    ) {
        try {
            db.collection("eventGroups")
                .document(groupId)
                .update(
                    mapOf(
                        "lastMessage" to lastMessage,
                        "lastMessageTime" to timestamp
                    )
                ).await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    // ==================== DIRECT/INDIVIDUAL MESSAGES ====================

    /**
     * Send direct message to another user
     */
    suspend fun sendDirectMessage(message: Message): Result<String> {
        return try {
            val messageRef = db.collection("directMessages").document()
            message.id = messageRef.id

            messageRef.set(message).await()

            // Create or update conversation
            updateConversation(message)

            Result.success(message.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Listen to direct messages in real-time
     */
    fun listenToDirectMessages(
        conversationId: String,
        onMessagesChanged: (List<Message>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("directMessages")
            .whereEqualTo("groupId", conversationId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val messages = snapshots?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Message::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                onMessagesChanged(messages)
            }
    }

    /**
     * Get or create conversation ID between two users
     */
    fun getConversationId(userId1: String, userId2: String): String {
        // Sort IDs to ensure consistent conversation ID
        val sortedIds = listOf(userId1, userId2).sorted()
        return "${sortedIds[0]}_${sortedIds[1]}"
    }

    /**
     * Update conversation metadata
     */
    private suspend fun updateConversation(message: Message) {
        try {
            val conversationId = message.groupId
            val conversationRef = db.collection("conversations").document(conversationId)

            val conversationData = hashMapOf(
                "id" to conversationId,
                "lastMessage" to message.content,
                "lastMessageTime" to message.timestamp,
                "lastSenderId" to message.senderId,
                "lastSenderName" to message.senderName
            )

            conversationRef.set(conversationData).await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    /**
     * Get all conversations for current user
     */
    suspend fun getUserConversations(): List<Conversation> {
        return try {
            val userId = getCurrentUserId()
            val conversations = mutableListOf<Conversation>()

            // Get all conversations where user is participant
            val docs = db.collection("conversations")
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .get()
                .await()

            docs.documents.forEach { doc ->
                val conversationId = doc.id
                if (conversationId.contains(userId)) {
                    val lastMessageTime = doc.getTimestamp("lastMessageTime")
                    val conversation = Conversation(
                        id = conversationId,
                        lastMessage = doc.getString("lastMessage") ?: "",
                        lastMessageTime = lastMessageTime?.toDate() ?: java.util.Date(),
                        lastSenderId = doc.getString("lastSenderId") ?: "",
                        lastSenderName = doc.getString("lastSenderName") ?: ""
                    )
                    conversations.add(conversation)
                }
            }

            conversations
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get other user's ID from conversation ID
     */
    fun getOtherUserId(conversationId: String, currentUserId: String): String {
        val userIds = conversationId.split("_")
        return if (userIds.size >= 2) {
            if (userIds[0] == currentUserId) userIds[1] else userIds[0]
        } else {
            ""
        }
    }

    /**
     * Get user details
     */
    suspend fun getUserDetails(userId: String): UserDetails {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            UserDetails(
                id = userId,
                name = doc.getString("name") ?: "User",
                email = doc.getString("email") ?: "",
                profileImageUrl = doc.getString("profileImageUrl") ?: ""
            )
        } catch (e: Exception) {
            UserDetails(userId, "User", "", "")
        }
    }

    // ==================== DATA CLASSES ====================

    data class Conversation(
        val id: String = "",
        val lastMessage: String = "",
        val lastMessageTime: java.util.Date = java.util.Date(),
        val lastSenderId: String = "",
        val lastSenderName: String = ""
    )

    data class UserDetails(
        val id: String = "",
        val name: String = "",
        val email: String = "",
        val profileImageUrl: String = ""
    )
}