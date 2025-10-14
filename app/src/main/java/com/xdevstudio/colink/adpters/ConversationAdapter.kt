package com.xdevstudio.colink.adpters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xdevstudio.colink.R
import com.xdevstudio.colink.utils.FirebaseMessageHelper
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(
    private val conversations: MutableList<ConversationItem>,
    private val onConversationClick: (ConversationItem) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    inner class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userInitial: TextView = itemView.findViewById(R.id.senderInitial)
        val userName: TextView = itemView.findViewById(R.id.groupName)
        val lastMessage: TextView = itemView.findViewById(R.id.lastMessage)
        val timestamp: TextView = itemView.findViewById(R.id.timestamp)

        fun bind(conversation: ConversationItem) {
            userInitial.text = conversation.otherUserName.firstOrNull()?.toString()?.uppercase() ?: "U"
            userName.text = conversation.otherUserName
            lastMessage.text = conversation.lastMessage
            timestamp.text = formatTimestamp(conversation.lastMessageTime)

            itemView.setOnClickListener {
                onConversationClick(conversation)
            }
        }

        private fun formatTimestamp(date: Date): String {
            val now = Calendar.getInstance()
            val messageTime = Calendar.getInstance().apply { time = date }

            return when {
                now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR) &&
                        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
                }
                now.get(Calendar.DAY_OF_YEAR) - 1 == messageTime.get(Calendar.DAY_OF_YEAR) &&
                        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
                    "Yesterday"
                }
                now.get(Calendar.WEEK_OF_YEAR) == messageTime.get(Calendar.WEEK_OF_YEAR) &&
                        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
                    SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
                }
                else -> {
                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        // Reusing actitviy_item_event_group layout for conversations
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_event_group, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(conversations[position])
    }

    override fun getItemCount(): Int = conversations.size

    fun updateConversations(newConversations: List<ConversationItem>) {
        conversations.clear()
        conversations.addAll(newConversations)
        notifyDataSetChanged()
    }
}

data class ConversationItem(
    val conversationId: String,
    val otherUserId: String,
    val otherUserName: String,
    val lastMessage: String,
    val lastMessageTime: Date
)