package com.xdevstudio.colink



import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xdevstudio.colink.adpters.MessageAdapter
import com.xdevstudio.colink.models.Message
// import com.xdevstudio.colink.utils.FirebaseMessageHelper
import kotlinx.coroutines.launch
import java.util.*

class GroupChatActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var groupNameText: TextView
    private lateinit var groupMembersText: TextView
    private lateinit var videoCallButton: ImageButton
    private lateinit var menuButton: ImageButton

    // Event info views
    private lateinit var eventDateTimeText: TextView
    private lateinit var eventLocationText: TextView
    private lateinit var fundProgressLayout: LinearLayout
    private lateinit var fundProgressText: TextView
    private lateinit var fundPercentageText: TextView
    private lateinit var fundProgressBar: ProgressBar

    // Action buttons
    private lateinit var contributeFundsButton: MaterialButton
    private lateinit var viewLocationButton: MaterialButton

    // Message views
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var attachButton: ImageButton
    private lateinit var sendButton: ImageButton

    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()

    private var groupId: String = ""
    private var groupName: String = ""
    private var currentUserId: String = ""
    private var currentUserName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        // Get group info from intent
        groupId = intent.getStringExtra("GROUP_ID") ?: ""
        groupName = intent.getStringExtra("GROUP_NAME") ?: "Group Chat"

        // Get current user info
        currentUserId = // FirebaseMessageHelper.getCurrentUserId()

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupMessageInput()
        setupActionButtons()

        // Load data
        loadGroupData()
        loadCurrentUserName()
        listenToMessages()
    }

    private fun initializeViews() {
        // Toolbar
        backButton = findViewById(R.id.backButton)
        groupNameText = findViewById(R.id.groupNameText)
        groupMembersText = findViewById(R.id.groupMembersText)
        videoCallButton = findViewById(R.id.videoCallButton)
        menuButton = findViewById(R.id.menuButton)

        // Event info
        eventDateTimeText = findViewById(R.id.eventDateTimeText)
        eventLocationText = findViewById(R.id.eventLocationText)
        fundProgressLayout = findViewById(R.id.fundProgressLayout)
        fundProgressText = findViewById(R.id.fundProgressText)
        fundPercentageText = findViewById(R.id.fundPercentageText)
        fundProgressBar = findViewById(R.id.fundProgressBar)

        // Action buttons
        contributeFundsButton = findViewById(R.id.contributeFundsButton)
        viewLocationButton = findViewById(R.id.viewLocationButton)

        // Messages
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        attachButton = findViewById(R.id.attachButton)
        sendButton = findViewById(R.id.sendButton)
    }

    private fun setupToolbar() {
        groupNameText.text = groupName

        backButton.setOnClickListener {
            finish()
        }

        videoCallButton.setOnClickListener {
            Toast.makeText(this, "Video call - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        menuButton.setOnClickListener {
            showGroupMenu()
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messages, currentUserId)

        messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@GroupChatActivity).apply {
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

    private fun setupActionButtons() {
        contributeFundsButton.setOnClickListener {
            showContributeDialog()
        }

        viewLocationButton.setOnClickListener {
            openLocationView()
        }
    }

    private fun loadGroupData() {
        // TODO: Load from Firebase Firestore
        groupMembersText.text = "5 members"
        eventDateTimeText.text = "Dec 25, 2025 at 7:00 PM"
        eventLocationText.text = "Sandton City"

        val currentFunds = 2500.0
        val targetFunds = 5000.0
        val percentage = ((currentFunds / targetFunds) * 100).toInt()

        fundProgressText.text = "R ${String.format("%.2f", currentFunds)} / R ${String.format("%.2f", targetFunds)}"
        fundPercentageText.text = "$percentage%"
        fundProgressBar.progress = percentage

        fundProgressLayout.visibility = if (targetFunds > 0) View.VISIBLE else View.GONE
    }

    private fun loadCurrentUserName() {
        lifecycleScope.launch {
            currentUserName = // FirebaseMessageHelper.getCurrentUserName()
        }
    }

    private fun listenToMessages() {
        // FirebaseMessageHelper.listenToGroupMessages(
            groupId = groupId,
            onMessagesChanged = { newMessages ->
                messages.clear()
                messages.addAll(newMessages)
                messageAdapter.notifyDataSetChanged()

                if (messages.isNotEmpty()) {
                    messagesRecyclerView.scrollToPosition(messages.size - 1)
                }

                // Mark messages as read
                markMessagesAsRead()
            },
            onError = { exception ->
                Toast.makeText(this, "Error loading messages: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun markMessagesAsRead() {
        lifecycleScope.launch {
            // FirebaseMessageHelper.markMessagesAsRead(groupId, currentUserId)
        }
    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString().trim()

        if (messageText.isEmpty()) {
            return
        }

        // Create new message
        val newMessage = Message(
            id = "", // Firebase will generate
            groupId = groupId,
            senderId = currentUserId,
            senderName = currentUserName,
            content = messageText,
            timestamp = Date(),
            type = Message.MessageType.TEXT,
            isRead = false
        )

        // Send to Firebase
        lifecycleScope.launch {
            val result = // FirebaseMessageHelper.sendGroupMessage(newMessage)

            result.onSuccess {
                // Message sent successfully
                messageInput.text.clear()
                messagesRecyclerView.scrollToPosition(messages.size - 1)
            }

            result.onFailure { exception ->
                Toast.makeText(
                    this@GroupChatActivity,
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
            groupId = groupId,
            senderId = currentUserId,
            senderName = currentUserName,
            content = "📍 Shared current location",
            timestamp = Date(),
            type = Message.MessageType.LOCATION,
            isRead = false
        )

        lifecycleScope.launch {
            // FirebaseMessageHelper.sendGroupMessage(locationMessage)
        }

        Toast.makeText(this, "Location shared with group", Toast.LENGTH_SHORT).show()
    }

    private fun showContributeDialog() {
        val input = EditText(this)
        input.hint = "Amount (R)"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(60, 20, 60, 0)
        container.addView(input)

        MaterialAlertDialogBuilder(this)
            .setTitle("Contribute to Fund")
            .setMessage("Enter the amount you want to contribute:")
            .setView(container)
            .setPositiveButton("Contribute") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull()
                if (amount != null && amount > 0) {
                    processContribution(amount)
                } else {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun processContribution(amount: Double) {
        // Send system message about contribution
        val contributionMessage = Message(
            id = "",
            groupId = groupId,
            senderId = "system",
            senderName = "System",
            content = "💰 ${currentUserName} contributed R ${String.format("%.2f", amount)} to the group fund",
            timestamp = Date(),
            type = Message.MessageType.PAYMENT
        )

        lifecycleScope.launch {
            // FirebaseMessageHelper.sendGroupMessage(contributionMessage)
        }

        // Update fund progress UI
        val currentAmount = fundProgressText.text.toString()
            .split("/")[0]
            .replace("R", "")
            .replace(",", "")
            .trim()
            .toDoubleOrNull() ?: 0.0

        val newAmount = currentAmount + amount
        val targetFunds = 5000.0
        val percentage = ((newAmount / targetFunds) * 100).toInt()

        fundProgressText.text = "R ${String.format("%.2f", newAmount)} / R ${String.format("%.2f", targetFunds)}"
        fundPercentageText.text = "$percentage%"
        fundProgressBar.progress = percentage

        Toast.makeText(this, "Contribution successful! Thank you!", Toast.LENGTH_SHORT).show()
    }

    private fun openLocationView() {
        Toast.makeText(this, "Opening location in maps...", Toast.LENGTH_SHORT).show()
        // Navigate to TrackingActivity
        // startActivity(Intent(this, TrackingActivity::class.java))
    }

    private fun showGroupMenu() {
        val options = arrayOf(
            "👥 Group Info",
            "🔔 Mute Notifications",
            "📎 Media & Files",
            "🎯 Event Details",
            "💰 Fund Management",
            "⚙️ Group Settings",
            "🚪 Exit Group"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Group Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showGroupInfo()
                    1 -> Toast.makeText(this, "Notifications - Coming Soon", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(this, "Media - Coming Soon", Toast.LENGTH_SHORT).show()
                    3 -> showEventDetails()
                    4 -> showFundManagement()
                    5 -> Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show()
                    6 -> confirmExitGroup()
                }
            }
            .show()
    }

    private fun showGroupInfo() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Group Information")
            .setMessage("""
                Group Name: $groupName
                Total Members: 5
                Created: Dec 10, 2025
                Event Date: Dec 25, 2025 at 7:00 PM
                Location: Sandton City
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showEventDetails() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Event Details")
            .setMessage("""
                📅 Date: Dec 25, 2025
                🕐 Time: 7:00 PM
                📍 Location: Sandton City
                💰 Budget: R 5,000
                ✅ Current Funds: R 2,500 (50%)
                
                Description:
                John's 25th birthday celebration!
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showFundManagement() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Fund Management")
            .setMessage("""
                💰 Total Budget: R 5,000
                ✅ Collected: R 2,500 (50%)
                ⏳ Remaining: R 2,500
                
                Recent Contributions:
                • John - R 500
                • Sarah - R 750
                • Mike - R 1,250
            """.trimIndent())
            .setPositiveButton("View Details", null)
            .setNegativeButton("Close", null)
            .setNeutralButton("Contribute") { _, _ ->
                showContributeDialog()
            }
            .show()
    }

    private fun confirmExitGroup() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit Group?")
            .setMessage("Are you sure you want to exit this group?")
            .setPositiveButton("Exit") { _, _ ->
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Mark messages as read when leaving
        markMessagesAsRead()
    }
}
