package com.xdevstudio.colink

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)

        // Find the New Group button and set click listener
        val newGroupButton = findViewById<TextView>(R.id.newGroupButton)
        newGroupButton.setOnClickListener {
            val intent = Intent(this, NewGroupActivity::class.java)
            startActivity(intent)
        }

        // You can also set up other click listeners for bottom navigation
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        // Add your bottom navigation logic here
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
}