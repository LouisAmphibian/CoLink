package com.xdevstudio.colink

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Add other settings navigation as needed
        findViewById<LinearLayout>(R.id.profileOption)?.setOnClickListener {
            val intent = Intent(this, ProfileCreationActivity::class.java)
            startActivity(intent)
        }
    }
}