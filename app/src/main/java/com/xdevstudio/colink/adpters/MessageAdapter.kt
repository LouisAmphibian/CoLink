package com.xdevstudio.colink.adpters



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.xdevstudio.colink.R
import com.xdevstudio.colink.models.Message
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val messages: MutableList<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Received message views
        val receivedMessageLayout: LinearLayout = itemView.findViewById(R.id.receivedMessageLayout)
        val senderInitial: TextView = itemView.findViewById(R.id.senderInitial)
        val senderNameText: TextView = itemView.findViewById(R.id.senderNameText)
        val receivedMessageText: TextView = itemView.findViewById(R.id.receivedMessageText)
        val receivedTimeText: TextView = itemView.findViewById(R.id.receivedTimeText)

        // Sent message views
        val sentMessageLayout: LinearLayout = itemView.findViewById(R.id.sentMessageLayout)
        val sentMessageText: TextView = itemView.findViewById(R.id.sentMessageText)
        val sentTimeText: TextView = itemView.findViewById(R.id.sentTimeText)
        val messageStatusIcon: ImageView = itemView.findViewById(R.id.messageStatusIcon)

        // System message views
        val systemMessageLayout: MaterialCardView = itemView.findViewById(R.id.systemMessageLayout)
        val systemMessageText: TextView = itemView.findViewById(R.id.systemMessageText)

        fun bind(message: Message) {
            when (message.type) {
                Message.MessageType.SYSTEM, Message.MessageType.PAYMENT -> {
                    // Show system message (center)
                    receivedMessageLayout.visibility = View.GONE
                    sentMessageLayout.visibility = View.GONE
                    systemMessageLayout.visibility = View.VISIBLE
                    systemMessageText.text = message.content
                }
                else -> {
                    systemMessageLayout.visibility = View.GONE

                    if (message.senderId == currentUserId) {
                        // Sent message (right side)
                        receivedMessageLayout.visibility = View.GONE
                        sentMessageLayout.visibility = View.VISIBLE

                        sentMessageText.text = message.content
                        sentTimeText.text = message.getFormattedTime()

                        // Update read status icon
                        if (message.isRead) {
                            messageStatusIcon.setImageResource(R.drawable.ic_done_all)
                            messageStatusIcon.setColorFilter(
                                itemView.context.getColor(R.color.active_green)
                            )
                        } else {
                            messageStatusIcon.setImageResource(R.drawable.ic_done)
                            messageStatusIcon.setColorFilter(
                                itemView.context.getColor(android.R.color.white)
                            )
                        }
                    } else {
                        // Received message (left side)
                        receivedMessageLayout.visibility = View.VISIBLE
                        sentMessageLayout.visibility = View.GONE

                        senderInitial.text = message.senderName.firstOrNull()?.toString()?.uppercase() ?: "?"
                        senderNameText.text = message.senderName
                        receivedMessageText.text = message.content
                        receivedTimeText.text = message.getFormattedTime()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun addMessages(newMessages: List<Message>) {
        val startPosition = messages.size
        messages.addAll(newMessages)
        notifyItemRangeInserted(startPosition, newMessages.size)
    }

    fun updateMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun markMessagesAsRead() {
        messages.forEach { it.isRead = true }
        notifyDataSetChanged()
    }
}