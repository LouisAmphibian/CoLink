package com.xdevstudio.colink


import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.xdevstudio.colink.adpters.EventGroupAdapter
import com.xdevstudio.colink.models.EventGroup
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var emptyStateView: LinearLayout
    private lateinit var newGroupButton: MaterialButton
    private lateinit var eventGroupAdapter: EventGroupAdapter

    private val eventGroups = mutableListOf<EventGroup>()


    private val createGroupLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadEventGroups()
            Toast.makeText(this, "Event group created!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)

        initializeViews()
        setupRecyclerView()
        setupSearchFunctionality()
        setupBottomNavigation()
        setupNewGroupButton()

        // Load groups (later needs to be replaced with Firebase data)
        loadEventGroups()

        // UNCOMMENT THIS LINE TO TEST WITH SAMPLE DATA
        addSampleGroups()
    }

    private fun initializeViews() {
        searchEditText = findViewById(R.id.searchEditText)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        emptyStateView = findViewById(R.id.emptyStateView)
        newGroupButton = findViewById(R.id.newGroupButton)
    }

    private fun setupRecyclerView() {
        eventGroupAdapter = EventGroupAdapter(eventGroups) { group ->
            openEventGroup(group)
        }

        chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = eventGroupAdapter
        }
    }

    private fun setupSearchFunctionality() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterGroups(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupBottomNavigation() {
        val directMessagesButton: ImageButton = findViewById(R.id.navDirect)
        val navHelp: ImageButton = findViewById(R.id.navHelp)
        val navChats: ImageButton = findViewById(R.id.navChats)
        val navSettings: ImageButton = findViewById(R.id.navSettings)
        val navSecurity: ImageButton = findViewById(R.id.navSecurity)

        directMessagesButton.setOnClickListener {
            startActivity(Intent(this, ConversationsActivity::class.java))
        }


        navHelp.setOnClickListener {
            Toast.makeText(this, "Help - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        navChats.setOnClickListener {
            // Already on chat screen
        }

        navSettings.setOnClickListener {
            try {
                startActivity(Intent(this, SettingsActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show()
            }
        }

        navSecurity.setOnClickListener {
            Toast.makeText(this, "Security - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupNewGroupButton() {
        newGroupButton.setOnClickListener {
            val intent = Intent(this, NewGroupActivity::class.java)
            createGroupLauncher.launch(intent)
        }
    }

    private fun loadEventGroups() {
        // TODO: Replace with Firebase Firestore data loading
        // For now, check if we have any groups

        if (eventGroups.isEmpty()) {
            // Show empty state
            showEmptyState(true)
        } else {
            showEmptyState(false)
        }
    }

    private fun showEmptyState(show: Boolean) {
        if (show) {
            emptyStateView.visibility = View.VISIBLE
            chatRecyclerView.visibility = View.GONE
        } else {
            emptyStateView.visibility = View.GONE
            chatRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun filterGroups(query: String) {
        eventGroupAdapter.filterGroups(query)
    }

    private fun openEventGroup(group: EventGroup) {
        // Open GroupChatActivity with group details
        val intent = Intent(this, GroupChatActivity::class.java).apply {
            putExtra("GROUP_ID", group.id)
            putExtra("GROUP_NAME", group.name)
        }
        startActivity(intent)
    }

    // For testing - add sample groups
    private fun addSampleGroups() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7)

        val sampleGroup = EventGroup(
            id = UUID.randomUUID().toString(),
            name = "Birthday Party",
            description = "John's 25th birthday celebration",
            eventDate = calendar.time,
            eventTime = "7:00 PM",
            location = "Sandton City",
            budget = 5000.0,
            currentFunds = 2500.0,
            lastMessage = "See you all there!",
            lastMessageTime = Date(),
            unreadCount = 3,
            hasFundingActive = true
        )

        // Add another sample group
        calendar.add(Calendar.DAY_OF_YEAR, 14)

        val sampleGroup2 = EventGroup(
            id = UUID.randomUUID().toString(),
            name = "Beach Picnic",
            description = "Weekend beach getaway",
            eventDate = calendar.time,
            eventTime = "10:00 AM",
            location = "Durban Beach",
            budget = 3000.0,
            currentFunds = 1500.0,
            lastMessage = "Don't forget sunscreen!",
            lastMessageTime = Date(),
            unreadCount = 1,
            hasFundingActive = true
        )

        eventGroups.add(sampleGroup)
        eventGroups.add(sampleGroup2)
        eventGroupAdapter.notifyDataSetChanged()
        showEmptyState(false)
    }
}