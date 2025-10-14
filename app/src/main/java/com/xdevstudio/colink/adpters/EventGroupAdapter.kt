package com.xdevstudio.colink.adpters



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xdevstudio.colink.R
import com.xdevstudio.colink.models.EventGroup
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class EventGroupAdapter(
    private var groups: MutableList<EventGroup>,
    private val onGroupClick: (EventGroup) -> Unit
) : RecyclerView.Adapter<EventGroupAdapter.EventGroupViewHolder>() {

    inner class EventGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupIcon: ImageView = itemView.findViewById(R.id.groupIcon)
        val groupName: TextView = itemView.findViewById(R.id.groupName)
        val lastMessage: TextView = itemView.findViewById(R.id.lastMessage)
        val eventDate: TextView = itemView.findViewById(R.id.eventDate)
        val timestamp: TextView = itemView.findViewById(R.id.timestamp)
        val unreadBadge: MaterialCardView = itemView.findViewById(R.id.unreadBadge)
        val unreadCount: TextView = itemView.findViewById(R.id.unreadCount)
        val fundStatusIcon: ImageView = itemView.findViewById(R.id.fundStatusIcon)

        fun bind(group: EventGroup) {
            groupName.text = group.name

            // Format last message
            lastMessage.text = if (group.lastMessage.isNotEmpty()) {
                group.lastMessage
            } else {
                "No messages yet"
            }

            // Format event date
            eventDate.text = group.getFormattedEventDateTime()

            // Format timestamp
            timestamp.text = formatTimestamp(group.lastMessageTime)

            // Show/hide unread badge
            if (group.unreadCount > 0) {
                unreadBadge.visibility = View.VISIBLE
                unreadCount.text = group.unreadCount.toString()
            } else {
                unreadBadge.visibility = View.GONE
            }

            // Show fund status icon if funding is active
            fundStatusIcon.visibility = if (group.hasFundingActive) {
                View.VISIBLE
            } else {
                View.GONE
            }

            // Click listener
            itemView.setOnClickListener {
                onGroupClick(group)
            }
        }

        private fun formatTimestamp(date: Date): String {
            val now = Calendar.getInstance()
            val messageTime = Calendar.getInstance().apply { time = date }

            return when {
                // Today
                now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR) &&
                        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
                }
                // Yesterday
                now.get(Calendar.DAY_OF_YEAR) - 1 == messageTime.get(Calendar.DAY_OF_YEAR) &&
                        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
                    "Yesterday"
                }
                // This week
                now.get(Calendar.WEEK_OF_YEAR) == messageTime.get(Calendar.WEEK_OF_YEAR) &&
                        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
                    SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
                }
                // Older
                else -> {
                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventGroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_event_group, parent, false)
        return EventGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventGroupViewHolder, position: Int) {
        holder.bind(groups[position])
    }

    override fun getItemCount(): Int = groups.size

    fun updateGroups(newGroups: List<EventGroup>) {
        groups.clear()
        groups.addAll(newGroups)
        notifyDataSetChanged()
    }

    fun addGroup(group: EventGroup) {
        groups.add(0, group)
        notifyItemInserted(0)
    }

    fun removeGroup(position: Int) {
        groups.removeAt(position)
        notifyItemRemoved(position)
    }

    fun filterGroups(query: String) {
        val filtered = if (query.isEmpty()) {
            groups
        } else {
            groups.filter { group ->
                group.name.contains(query, ignoreCase = true) ||
                        group.description.contains(query, ignoreCase = true) ||
                        group.location.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}