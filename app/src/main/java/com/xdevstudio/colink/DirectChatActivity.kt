package com.xdevstudio.colink


import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xdevstudio.colink.adpters.MessageAdapter
import com.xdevstudio.colink.models.Message
import com.xdevstudio.colink.utils.FirebaseMessageHelper
import kotlinx.coroutines.launch
import java.util.*

class DirectChatActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var userInitial: TextView
    private lateinit var userNameText: TextView
    private lateinit var userStatusText: TextView
    private lateinit var videoCallButton: ImageButton
    private lateinit var menuButton: ImageButton

    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var attachButton: ImageButton
    private lateinit var sendButton: ImageButton

    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()

    private var otherUserId: String = ""
    private var otherUserName: String = ""
    private var conversationId: String = ""
    private var currentUserId: String = ""
    private var currentUserName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direct_chat)

        // Get user info from intent
        otherUserId = intent.getStringExtra("USER_ID") ?: ""
        otherUserName = intent.getStringExtra("USER_NAME") ?: "User"

        // Get current user info
        currentUserId = FirebaseMessageHelper.getCurrentUserId()

        // Generate conversation ID
        conversationId = FirebaseMessageHelper.getConversationId(currentUserId, otherUserId)

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupMessageInput()

        loadCurrentUserName()
        loadOtherUserDetails()
        listenToMessages()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        userInitial = findViewById(R.id.userInitial)
        userNameText = findViewById(R.id.userNameText)
        userStatusText = findViewById(R.id.userStatusText)
        videoCallButton = findViewById(R.id.videoCallButton)
        menuButton = findViewById(R.id.menuButton)

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        attachButton = findViewById(R.id.attachButton)
        sendButton = findViewById(R.id.sendButton)
    }

    private fun setupToolbar() {
        userNameText.text = otherUserName
        userInitial.text = otherUserName.firstOrNull()?.toString()?.uppercase() ?: "U"

        backButton.setOnClickListener {
            finish()
        }

        videoCallButton.setOnClickListener {
            Toast.makeText(this, "Video call - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        menuButton.setOnClickListener {
            showChatMenu()
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messages, currentUserId)

        messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DirectChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun setupMessageInput() {
        sendButton.setOnClickListener {
            sendMessage()
        }

        attachButton.setOnClickListener {
            showAttachmentOptions()
        }

        // Enable/disable send button based on input
        messageInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sendButton.isEnabled = s?.trim()?.isNotEmpty() == true
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        sendButton.isEnabled = false
    }

    private fun loadCurrentUserName() {
        lifecycleScope.launch {
            currentUserName = FirebaseMessageHelper.getCurrentUserName()
        }
    }

    private fun loadOtherUserDetails() {
        lifecycleScope.launch {
            val userDetails = FirebaseMessageHelper.getUserDetails(otherUserId)
            userNameText.text = userDetails.name
            userInitial.text = userDetails.name.firstOrNull()?.toString()?.uppercase() ?: "U"
            otherUserName = userDetails.name

            // Update status (can be enhanced with real-time presence)
            userStatusText.text = "Tap to view profile"
        }
    }

    private fun listenToMessages() {
        FirebaseMessageHelper.listenToDirectMessages(
            conversationId = conversationId,
            onMessagesChanged = { newMessages ->
                messages.clear()
                messages.addAll(newMessages)
                messageAdapter.notifyDataSetChanged()

                if (messages.isNotEmpty()) {
                    messagesRecyclerView.scrollToPosition(messages.size - 1)
                }
            },
            onError = { exception ->
                Toast.makeText(this, "Error loading messages: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString().trim()

        if (messageText.isEmpty()) {
            return
        }

        // Create new message (using groupId field for conversationId)
        val newMessage = Message(
            id = "", // Firebase will generate
            groupId = conversationId,
            senderId = currentUserId,
            senderName = currentUserName,
            content = messageText,
            timestamp = Date(),
            type = Message.MessageType.TEXT,
            isRead = false
        )

        // Send to Firebase
        lifecycleScope.launch {
            val result = FirebaseMessageHelper.sendDirectMessage(newMessage)

            result.onSuccess {
                // Message sent successfully
                messageInput.text.clear()
                messagesRecyclerView.scrollToPosition(messages.size - 1)
            }

            result.onFailure { exception ->
                Toast.makeText(
                    this@DirectChatActivity,
                    "Failed to send: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showAttachmentOptions() {
        val options = arrayOf(
            "📷 Camera",
            "🖼️ Gallery",
            "📍 Location",
            "📄 Document"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Attach")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> Toast.makeText(this, "Camera - Coming Soon", Toast.LENGTH_SHORT).show()
                    1 -> Toast.makeText(this, "Gallery - Coming Soon", Toast.LENGTH_SHORT).show()
                    2 -> shareLocation()
                    3 -> Toast.makeText(this, "Documents - Coming Soon", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun shareLocation() {
        val locationMessage = Message(
            id = "",
            groupId = conversationId,
            senderId = currentUserId,
            senderName = currentUserName,
            content = "📍 Shared current location",
            timestamp = Date(),
            type = Message.MessageType.LOCATION,
            isRead = false
        )

        lifecycleScope.launch {
            FirebaseMessageHelper.sendDirectMessage(locationMessage)
        }

        Toast.makeText(this, "Location shared", Toast.LENGTH_SHORT).show()
    }

    private fun showChatMenu() {
        val options = arrayOf(
            "👤 View Profile",
            "🔔 Mute Notifications",
            "📎 Media & Files",
            "🔍 Search Messages",
            "🗑️ Clear Chat",
            "🚫 Block User"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Chat Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewUserProfile()
                    1 -> Toast.makeText(this, "Mute - Coming Soon", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(this, "Media - Coming Soon", Toast.LENGTH_SHORT).show()
                    3 -> Toast.makeText(this, "Search - Coming Soon", Toast.LENGTH_SHORT).show()
                    4 -> confirmClearChat()
                    5 -> confirmBlockUser()
                }
            }
            .show()
    }

    private fun viewUserProfile() {
        MaterialAlertDialogBuilder(this)
            .setTitle("User Profile")
            .setMessage("""
                Name: $otherUserName
                
                Member since: January 2025
                Events attended: 12
                Reward points: 850
                
                Bio: Event enthusiast | Always up for fun gatherings!
            """.trimIndent())
            .setPositiveButton("Close", null)
            .setNeutralButton("Send Event Invite") { _, _ ->
                Toast.makeText(this, "Invite - Coming Soon", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun confirmClearChat() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Clear Chat?")
            .setMessage("This will delete all messages in this conversation. This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                Toast.makeText(this, "Chat cleared", Toast.LENGTH_SHORT).show()
                // TODO: Implement delete messages from Firebase
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmBlockUser() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Block $otherUserName?")
            .setMessage("You won't receive messages from this user. You can unblock them later from settings.")
            .setPositiveButton("Block") { _, _ ->
                Toast.makeText(this, "User blocked", Toast.LENGTH_SHORT).show()
                finish()
                // TODO: Implement block user functionality
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}