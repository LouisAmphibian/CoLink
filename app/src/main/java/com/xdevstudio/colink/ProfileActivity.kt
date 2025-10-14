package com.xdevstudio.colink


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class ProfileActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var editButton: ImageButton
    private lateinit var profileInitial: TextView
    private lateinit var changePhotoButton: Button
    private lateinit var nameText: TextView
    private lateinit var emailText: TextView
    private lateinit var phoneText: TextView
    private lateinit var rewardPointsText: TextView
    private lateinit var viewRewardsButton: ImageButton
    private lateinit var bioText: TextView

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        prefs = getSharedPreferences("CoLinkPrefs", MODE_PRIVATE)

        initializeViews()
        setupClickListeners()
        loadUserData()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        editButton = findViewById(R.id.editButton)
        profileInitial = findViewById(R.id.profileInitial)
        changePhotoButton = findViewById(R.id.changePhotoButton)
        nameText = findViewById(R.id.nameText)
        emailText = findViewById(R.id.emailText)
        phoneText = findViewById(R.id.phoneText)
        rewardPointsText = findViewById(R.id.rewardPointsText)
        viewRewardsButton = findViewById(R.id.viewRewardsButton)
        bioText = findViewById(R.id.bioText)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        editButton.setOnClickListener {
            showEditProfileDialog()
        }

        changePhotoButton.setOnClickListener {
            changeProfilePhoto()
        }

        viewRewardsButton.setOnClickListener {
            showRewardsDialog()
        }
    }

    private fun loadUserData() {
        // TODO: Load from Firebase Firestore
        // For now, using SharedPreferences for demo

        val userName = prefs.getString("user_name", "John Doe") ?: "John Doe"
        val userEmail = prefs.getString("user_email", "john.doe@example.com") ?: "john.doe@example.com"
        val userPhone = prefs.getString("user_phone", "+27 123 456 789") ?: "+27 123 456 789"
        val userBio = prefs.getString("user_bio", "Event enthusiast | Love organizing get-togethers | Always up for an adventure!")
            ?: "Event enthusiast | Love organizing get-togethers | Always up for an adventure!"
        val rewardPoints = prefs.getInt("reward_points", 1250)

        nameText.text = userName
        emailText.text = userEmail
        phoneText.text = userPhone
        bioText.text = userBio
        rewardPointsText.text = "$rewardPoints Points"

        // Set profile initial
        val initial = userName.firstOrNull()?.toString()?.uppercase() ?: "U"
        profileInitial.text = initial
    }

    private fun showEditProfileDialog() {
        val options = arrayOf(
            "Edit Name",
            "Edit Bio",
            "Change Email",
            "Change Phone"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Profile")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditNameDialog()
                    1 -> showEditBioDialog()
                    2 -> showEditEmailDialog()
                    3 -> showEditPhoneDialog()
                }
            }
            .show()
    }

    private fun showEditNameDialog() {
        val input = TextInputEditText(this)
        input.setText(nameText.text)

        val container = android.widget.LinearLayout(this)
        container.orientation = android.widget.LinearLayout.VERTICAL
        container.setPadding(60, 20, 60, 0)
        container.addView(input)

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Name")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    prefs.edit().putString("user_name", newName).apply()
                    nameText.text = newName
                    profileInitial.text = newName.firstOrNull()?.toString()?.uppercase() ?: "U"
                    Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditBioDialog() {
        val input = TextInputEditText(this)
        input.setText(bioText.text)
        input.minLines = 3
        input.maxLines = 5

        val container = android.widget.LinearLayout(this)
        container.orientation = android.widget.LinearLayout.VERTICAL
        container.setPadding(60, 20, 60, 0)
        container.addView(input)

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Bio")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val newBio = input.text.toString().trim()
                if (newBio.isNotEmpty()) {
                    prefs.edit().putString("user_bio", newBio).apply()
                    bioText.text = newBio
                    Toast.makeText(this, "Bio updated", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditEmailDialog() {
        Toast.makeText(this, "Email change requires verification - Coming Soon", Toast.LENGTH_SHORT).show()
        // TODO: Implement with Firebase Auth email verification
    }

    private fun showEditPhoneDialog() {
        Toast.makeText(this, "Phone change requires verification - Coming Soon", Toast.LENGTH_SHORT).show()
        // TODO: Implement with Firebase Auth phone verification
    }

    private fun changeProfilePhoto() {
        val options = arrayOf(
            "Take Photo",
            "Choose from Gallery",
            "Remove Photo"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Profile Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        Toast.makeText(this, "Camera - Coming Soon", Toast.LENGTH_SHORT).show()
                        // TODO: Open camera
                    }
                    1 -> {
                        Toast.makeText(this, "Gallery - Coming Soon", Toast.LENGTH_SHORT).show()
                        // TODO: Open gallery
                    }
                    2 -> {
                        Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show()
                        // Reset to initial
                    }
                }
            }
            .show()
    }

    private fun showRewardsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Your Rewards & Points")
            .setMessage("""
                💰 Total Points: ${prefs.getInt("reward_points", 1250)}
                
                Recent Activities:
                ✅ Created Birthday Party - 100 pts
                ✅ Contributed to Beach Picnic - 150 pts
                ✅ Attended 3 events this month - 300 pts
                ✅ Invited 5 friends - 250 pts
                
                Rewards You Can Redeem:
                🎁 R50 Event Discount (500 pts)
                🎁 R100 Event Discount (900 pts)
                🎁 Premium Badge (1500 pts)
                🎁 Free Event Planning (2000 pts)
                
                Keep participating to earn more points!
            """.trimIndent())
            .setPositiveButton("Redeem Points") { _, _ ->
                showRedeemDialog()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showRedeemDialog() {
        val currentPoints = prefs.getInt("reward_points", 1250)

        val rewards = arrayOf(
            "R50 Event Discount (500 pts)",
            "R100 Event Discount (900 pts)",
            "Premium Badge (1500 pts)",
            "Free Event Planning (2000 pts)"
        )

        val costs = arrayOf(500, 900, 1500, 2000)

        MaterialAlertDialogBuilder(this)
            .setTitle("Redeem Rewards")
            .setMessage("You have $currentPoints points")
            .setItems(rewards) { _, which ->
                val cost = costs[which]
                if (currentPoints >= cost) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Confirm Redemption")
                        .setMessage("Redeem ${rewards[which]}?")
                        .setPositiveButton("Redeem") { _, _ ->
                            val newPoints = currentPoints - cost
                            prefs.edit().putInt("reward_points", newPoints).apply()
                            rewardPointsText.text = "$newPoints Points"
                            Toast.makeText(this, "Reward redeemed successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    Toast.makeText(this, "Not enough points. Need ${cost - currentPoints} more.", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}