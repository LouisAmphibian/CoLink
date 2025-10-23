package com.xdevstudio.colink

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(androidx.appcompat.R.id.action_bar_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Show splash screen for 3 seconds, then check user status
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserStatus()
        }, 3000) // 3 seconds splash screen
    }

    private fun checkUserStatus() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is authenticated, check if profile exists
            checkUserProfileExists(currentUser.uid)
        } else {
            // No user authenticated, start from language selection
            navigateToLanguageActivity()
        }
    }

    private fun checkUserProfileExists(userId: String) {
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // User has profile, go directly to chat
                    println("✅ User profile exists, navigating to ChatActivity")
                    navigateToChatActivity()
                } else {
                    // User authenticated but no profile, go to profile creation
                    println("⚠️ User authenticated but no profile, navigating to ProfileCreation")
                    navigateToProfileCreationActivity()
                }
            }
            .addOnFailureListener { exception ->
                // If there's an error, default to language selection for safety
                println("❌ Error checking profile: ${exception.message}")
                navigateToLanguageActivity()
            }
    }

    private fun navigateToLanguageActivity() {
        val intent = Intent(this, LanguageActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToProfileCreationActivity() {
        val intent = Intent(this, ProfileCreationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToChatActivity() {
        val intent = Intent(this, ChatActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}