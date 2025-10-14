package com.xdevstudio.colink

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xdevstudio.colink.adpters.ConversationAdapter
import com.xdevstudio.colink.adpters.ConversationItem
import com.xdevstudio.colink.utils.FirebaseMessageHelper
import kotlinx.coroutines.launch

class ConversationsActivity : AppCompatActivity() {

    private lateinit var newChatButton: ImageButton
    private lateinit var searchEditText: EditText
    private lateinit var conversationsRecyclerView: RecyclerView
    private lateinit var emptyStateView: LinearLayout

    private lateinit var conversationAdapter: ConversationAdapter
    private val conversations = mutableListOf<ConversationItem>()

    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)

        currentUserId = FirebaseMessageHelper.getCurrentUserId()

        initializeViews()
        setupRecyclerView()
        setupClickListeners()

        loadConversations()
    }

    private fun initializeViews() {
        newChatButton = findViewById(R.id.newChatButton)
        searchEditText = findViewById(R.id.searchEditText)
        conversationsRecyclerView = findViewById(R.id.conversationsRecyclerView)
        emptyStateView = findViewById(R.id.emptyStateView)
    }

    private fun setupRecyclerView() {
        conversationAdapter = ConversationAdapter(conversations) { conversation ->
            openDirectChat(conversation)
        }

        conversationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ConversationsActivity)
            adapter = conversationAdapter
        }
    }

    private fun setupClickListeners() {
        newChatButton.setOnClickListener {
            showNewChatDialog()
        }

        // Search functionality
        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterConversations(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun loadConversations() {
        lifecycleScope.launch {
            try {
                val firebaseConversations = FirebaseMessageHelper.getUserConversations()

                conversations.clear()

                for (conversation in firebaseConversations) {
                    val otherUserId = FirebaseMessageHelper.getOtherUserId(
                        conversation.id,
                        currentUserId
                    )

                    val userDetails = FirebaseMessageHelper.getUserDetails(otherUserId)

                    conversations.add(
                        ConversationItem(
                            conversationId = conversation.id,
                            otherUserId = otherUserId,
                            otherUserName = userDetails.name,
                            lastMessage = conversation.lastMessage,
                            lastMessageTime = conversation.lastMessageTime
                        )
                    )
                }

                conversationAdapter.notifyDataSetChanged()
                showEmptyState(conversations.isEmpty())

            } catch (e: Exception) {
                Toast.makeText(
                    this@ConversationsActivity,
                    "Error loading conversations: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                showEmptyState(true)
            }
        }
    }

    private fun showEmptyState(show: Boolean) {
        if (show) {
            emptyStateView.visibility = View.VISIBLE
            conversationsRecyclerView.visibility = View.GONE
        } else {
            emptyStateView.visibility = View.GONE
            conversationsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun filterConversations(query: String) {
        if (query.isEmpty()) {
            conversationAdapter.notifyDataSetChanged()
            return
        }

        val filtered = conversations.filter { conversation ->
            conversation.otherUserName.contains(query, ignoreCase = true) ||
                    conversation.lastMessage.contains(query, ignoreCase = true)
        }

        conversationAdapter.updateConversations(filtered)
    }

    private fun openDirectChat(conversation: ConversationItem) {
        val intent = Intent(this, DirectChatActivity::class.java).apply {
            putExtra("USER_ID", conversation.otherUserId)
            putExtra("USER_NAME", conversation.otherUserName)
        }
        startActivity(intent)
    }

    private fun showNewChatDialog() {
        // For demo purposes - showing a simple input dialog
        // In production, you'd show a list of contacts/users

        val input = EditText(this)
        input.hint = "Enter user ID or select from contacts"

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(60, 20, 60, 0)
        container.addView(input)

        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Start New Chat")
            .setMessage("Enter the user ID to start a conversation:")
            .setView(container)
            .setPositiveButton("Start Chat") { _, _ ->
                val userId = input.text.toString().trim()
                if (userId.isNotEmpty() && userId != currentUserId) {
                    startNewChat(userId)
                } else {
                    Toast.makeText(this, "Please enter a valid user ID", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Browse Contacts") { _, _ ->
                Toast.makeText(this, "Contact list - Coming Soon", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun startNewChat(otherUserId: String) {
        lifecycleScope.launch {
            val userDetails = FirebaseMessageHelper.getUserDetails(otherUserId)

            val intent = Intent(this@ConversationsActivity, DirectChatActivity::class.java).apply {
                putExtra("USER_ID", otherUserId)
                putExtra("USER_NAME", userDetails.name)
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload conversations when returning to this activity
        loadConversations()
    }
}