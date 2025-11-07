package com.xdevstudio.colink

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*

class GroupChatActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var groupId: String

    private lateinit var groupNameText: TextView
    private lateinit var groupStatusText: TextView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var editEventButton: ImageButton
    private lateinit var eventInfoCard: com.google.android.material.card.MaterialCardView

    private var groupListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        groupId = intent.getStringExtra("groupId") ?: ""

        initializeViews()
        setupClickListeners()
        loadGroupData()
    }

    private fun initializeViews() {
        groupNameText = findViewById(R.id.groupNameText)
        groupStatusText = findViewById(R.id.groupStatusText)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        editEventButton = findViewById(R.id.editEventButton)
        eventInfoCard = findViewById(R.id.eventInfoCard)

        messagesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initially disable chat
        messageInput.isEnabled = false
        sendButton.isEnabled = false
        messageInput.hint = "Chat will be enabled when 2+ members join"
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener { sendMessage() }
        editEventButton.setOnClickListener { editEventDetails() }

        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }

        // Event info buttons
        findViewById<Button>(R.id.viewLocationButton)?.setOnClickListener {
            Toast.makeText(this, "View location feature", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.contributeFundsButton)?.setOnClickListener {
            Toast.makeText(this, "Contribute funds feature", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadGroupData() {
        groupListener = firestore.collection("groups")
            .document(groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading group: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val group = snapshot.toObject(Group::class.java)?.copy(id = snapshot.id)
                    group?.let {
                        updateUI(it)
                    }
                }
            }
    }

    private fun updateUI(group: Group) {
        groupNameText.text = group.name

        // Calculate accepted members
        val acceptedExistingMembers = group.members.count { it.accepted }
        val acceptedInvitedMembers = group.invitedMembers.count { it.status == "accepted" }
        val totalAccepted = acceptedExistingMembers + acceptedInvitedMembers
        val totalExpected = group.members.size + group.invitedMembers.size

        when {
            totalAccepted >= 2 -> {
                groupStatusText.text = "$totalAccepted members"
                messageInput.isEnabled = true
                sendButton.isEnabled = true
                messageInput.hint = "Type a message..."

                // Update group status in Firestore if needed
                if (group.status != "active") {
                    firestore.collection("groups").document(groupId)
                        .update("status", "active", "canChat", true)
                }
            }
            else -> {
                groupStatusText.text = "Pending: $totalAccepted/$totalExpected members accepted"
                messageInput.isEnabled = false
                sendButton.isEnabled = false
                messageInput.hint = "Chat will be enabled when 2+ members join"
            }
        }

        // Show edit button only for admin
        val currentUser = auth.currentUser
        editEventButton.visibility = if (currentUser?.uid == group.createdBy) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }

        // Update event info card
        updateEventInfoCard(group)
    }

    private fun updateEventInfoCard(group: Group) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val eventDateText = findViewById<TextView>(R.id.eventDateTimeText)
        val eventLocationText = findViewById<TextView>(R.id.eventLocationText)
        val fundProgressText = findViewById<TextView>(R.id.fundProgressText)
        val fundPercentageText = findViewById<TextView>(R.id.fundPercentageText)
        val fundProgressBar = findViewById<ProgressBar>(R.id.fundProgressBar)
        val fundProgressLayout = findViewById<LinearLayout>(R.id.fundProgressLayout)

        eventDateText?.text = "${dateFormat.format(group.date)} at ${group.time}"
        eventLocationText?.text = group.location

        // Show budget if set
        if (group.budget > 0) {
            fundProgressLayout?.visibility = android.view.View.VISIBLE
            fundProgressText?.text = "R ${String.format("%,.2f", group.budget)}"
            fundPercentageText?.text = "Target"
            fundProgressBar?.progress = 0
        } else {
            fundProgressLayout?.visibility = android.view.View.GONE
        }
    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString().trim()
        if (messageText.isEmpty()) return

        val currentUser = auth.currentUser ?: return

        val message = mapOf(
            "groupId" to groupId,
            "senderId" to currentUser.uid,
            "text" to messageText,
            "type" to "text",
            "timestamp" to Date(),
            "isSystemMessage" to false
        )

        firestore.collection("groups")
            .document(groupId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                messageInput.text.clear()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editEventDetails() {
        val intent = Intent(this, EditGroupActivity::class.java)
        intent.putExtra("groupId", groupId)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        groupListener?.remove()
    }
}