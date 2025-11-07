package com.xdevstudio.colink

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ChatActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: GroupAdapter
    private var groupsListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initializeViews()
        setupClickListeners()
        setupBottomNavigation()
        loadUserGroups()
    }

    private fun initializeViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatAdapter = GroupAdapter(emptyList()) { group ->
            openGroupChat(group)
        }
        chatRecyclerView.adapter = chatAdapter
    }

    private fun loadUserGroups() {
        val currentUser = auth.currentUser
        if (currentUser == null) return

        groupsListener = firestore.collection("groups")
            .whereArrayContains("members", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                val groups = mutableListOf<Group>()
                snapshot?.documents?.forEach { document ->
                    val group = document.toObject(Group::class.java)?.copy(id = document.id)
                    group?.let {
                        groups.add(it)
                    }
                }

                chatAdapter.updateGroups(groups)

                // Hide placeholder if groups exist
                val placeholder = findViewById<TextView>(R.id.chatPlaceholder)
                placeholder.visibility = if (groups.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
    }

    private fun openGroupChat(group: Group) {
        val intent = Intent(this, GroupChatActivity::class.java)
        intent.putExtra("groupId", group.id)
        startActivity(intent)
    }

    private fun setupClickListeners() {
        val newGroupButton = findViewById<TextView>(R.id.newGroupButton)
        newGroupButton.setOnClickListener {
            val intent = Intent(this, NewGroupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        findViewById<ImageButton>(R.id.navHelp)?.setOnClickListener {
            // Handle help navigation
        }

        findViewById<ImageButton>(R.id.navChats)?.setOnClickListener {
            // Already in chats
        }

        findViewById<ImageButton>(R.id.navSettings)?.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.navSecurity)?.setOnClickListener {
            // Handle security navigation
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        groupsListener?.remove()
    }
}

class GroupAdapter(
    private var groups: List<Group>,
    private val onGroupClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val groupName: TextView = itemView.findViewById(R.id.groupName)
        val groupStatus: TextView = itemView.findViewById(R.id.groupStatus)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): GroupViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.groupName.text = group.name

        val acceptedMembers = group.members.count { it.accepted }
        val totalMembers = group.members.size + group.invitedMembers.size
        holder.groupStatus.text = "$acceptedMembers/$totalMembers members"

        holder.itemView.setOnClickListener {
            onGroupClick(group)
        }
    }

    override fun getItemCount() = groups.size

    fun updateGroups(newGroups: List<Group>) {
        this.groups = newGroups
        notifyDataSetChanged()
    }
}