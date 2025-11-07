package com.xdevstudio.colink

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var profileOption: LinearLayout
    private lateinit var chatsOption: LinearLayout
    private lateinit var notificationsOption: LinearLayout
    private lateinit var privacyOption: LinearLayout
    private lateinit var accountOption: LinearLayout
    private lateinit var locationOption: LinearLayout
    private lateinit var helpOption: LinearLayout
    private lateinit var languageOption: LinearLayout
    private lateinit var aboutOption: LinearLayout
    private lateinit var logoutButton: MaterialButton

    private lateinit var profileIcon: TextView
    private lateinit var currentLanguageText: TextView

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("CoLinkPrefs", MODE_PRIVATE)

        initializeViews()
        setupClickListeners()
        loadUserProfile()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        profileOption = findViewById(R.id.profileOption)
        chatsOption = findViewById(R.id.chatsOption)
        notificationsOption = findViewById(R.id.notificationsOption)
        privacyOption = findViewById(R.id.privacyOption)
        accountOption = findViewById(R.id.accountOption)
        locationOption = findViewById(R.id.locationOption)
        helpOption = findViewById(R.id.helpOption)
        languageOption = findViewById(R.id.languageOption)
        aboutOption = findViewById(R.id.aboutOption)
        logoutButton = findViewById(R.id.logoutButton)

        profileIcon = findViewById(R.id.profileIcon)
        currentLanguageText = findViewById(R.id.currentLanguageText)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        profileOption.setOnClickListener {
            openProfile()
        }

        chatsOption.setOnClickListener {
            openChats()
        }

        notificationsOption.setOnClickListener {
            openNotifications()
        }

        privacyOption.setOnClickListener {
            openPrivacySettings()
        }

        accountOption.setOnClickListener {
            openAccountSettings()
        }

        locationOption.setOnClickListener {
            openLocationServices()
        }

        helpOption.setOnClickListener {
            openHelp()
        }

        languageOption.setOnClickListener {
            showLanguageDialog()
        }

        aboutOption.setOnClickListener {
            showAboutDialog()
        }

        logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun loadUserProfile() {
        // TODO: Load from Firebase Auth
        val userName = prefs.getString("user_name", "User") ?: "User"
        val initial = userName.firstOrNull()?.toString()?.uppercase() ?: "U"
        profileIcon.text = initial

        val currentLanguage = prefs.getString("language", "English") ?: "English"
        currentLanguageText.text = currentLanguage
    }

    private fun openProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun openChats() {
        val intent = Intent(this, ChatActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun openNotifications() {
        val options = arrayOf(
            "Push Notifications",
            "Event Reminders",
            "Payment Alerts",
            "Weather Alerts",
            "Group Messages",
            "Location Updates"
        )

        val checkedItems = booleanArrayOf(
            prefs.getBoolean("notif_push", true),
            prefs.getBoolean("notif_reminders", true),
            prefs.getBoolean("notif_payments", true),
            prefs.getBoolean("notif_weather", true),
            prefs.getBoolean("notif_messages", true),
            prefs.getBoolean("notif_location", false)
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Notification Settings")
            .setMultiChoiceItems(options, checkedItems) { _, which, isChecked ->
                when (which) {
                    0 -> prefs.edit().putBoolean("notif_push", isChecked).apply()
                    1 -> prefs.edit().putBoolean("notif_reminders", isChecked).apply()
                    2 -> prefs.edit().putBoolean("notif_payments", isChecked).apply()
                    3 -> prefs.edit().putBoolean("notif_weather", isChecked).apply()
                    4 -> prefs.edit().putBoolean("notif_messages", isChecked).apply()
                    5 -> prefs.edit().putBoolean("notif_location", isChecked).apply()
                }
            }
            .setPositiveButton("Save") { _, _ ->
                Toast.makeText(this, "Notification settings saved", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openPrivacySettings() {
        val options = arrayOf(
            "Location Sharing",
            "Profile Visibility",
            "Activity Status",
            "Read Receipts",
            "Biometric Authentication"
        )

        val checkedItems = booleanArrayOf(
            prefs.getBoolean("privacy_location", false),
            prefs.getBoolean("privacy_profile", true),
            prefs.getBoolean("privacy_status", true),
            prefs.getBoolean("privacy_read", true),
            prefs.getBoolean("privacy_biometric", false)
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Privacy & Security Settings")
            .setMultiChoiceItems(options, checkedItems) { _, which, isChecked ->
                when (which) {
                    0 -> prefs.edit().putBoolean("privacy_location", isChecked).apply()
                    1 -> prefs.edit().putBoolean("privacy_profile", isChecked).apply()
                    2 -> prefs.edit().putBoolean("privacy_status", isChecked).apply()
                    3 -> prefs.edit().putBoolean("privacy_read", isChecked).apply()
                    4 -> {
                        prefs.edit().putBoolean("privacy_biometric", isChecked).apply()
                        if (isChecked) {
                            Toast.makeText(this, "Biometric auth enabled for payments", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .setPositiveButton("Save") { _, _ ->
                Toast.makeText(this, "Privacy settings saved", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Data Protection") { _, _ ->
                showDataProtectionInfo()
            }
            .show()
    }

    private fun showDataProtectionInfo() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Data Protection & POPIA Compliance")
            .setMessage("""
                CoLink is committed to protecting your privacy and complying with POPIA (Protection of Personal Information Act).
                
                • Your data is encrypted end-to-end
                • We never share your personal information
                • You have the right to access your data
                • You can delete your account anytime
                • Location data is only shared with consent
                • Payment information is secured with biometric approval
                
                For full privacy policy, visit:
                www.colink.com/privacy
            """.trimIndent())
            .setPositiveButton("I Understand", null)
            .show()
    }

    private fun openAccountSettings() {
        val options = arrayOf(
            "Change Password",
            "Phone Number",
            "Email Address",
            "Delete Account"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Account Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showChangePasswordDialog()
                    1 -> showChangePhoneDialog()
                    2 -> showChangeEmailDialog()
                    3 -> showDeleteAccountConfirmation()
                }
            }
            .show()
    }

    private fun showChangePasswordDialog() {
        Toast.makeText(this, "Change Password - Coming Soon", Toast.LENGTH_SHORT).show()
        // TODO: Implement password change with Firebase Auth
    }

    private fun showChangePhoneDialog() {
        Toast.makeText(this, "Change Phone - Coming Soon", Toast.LENGTH_SHORT).show()
        // TODO: Implement phone change with Firebase Auth
    }

    private fun showChangeEmailDialog() {
        Toast.makeText(this, "Change Email - Coming Soon", Toast.LENGTH_SHORT).show()
        // TODO: Implement email change with Firebase Auth
    }

    private fun showDeleteAccountConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Account?")
            .setMessage("This action cannot be undone. All your data, including event groups, messages, and rewards will be permanently deleted.")
            .setPositiveButton("Delete") { _, _ ->
                Toast.makeText(this, "Account deletion - Coming Soon", Toast.LENGTH_SHORT).show()
                // TODO: Implement account deletion
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openLocationServices() {
        // Navigate to TrackingActivity
        val intent = Intent(this, TrackingActivity::class.java)
        startActivity(intent)
    }

    private fun openHelp() {
        val options = arrayOf(
            "📖 User Guide",
            "❓ FAQs",
            "💬 Contact Support",
            "🐛 Report a Bug",
            "⭐ Rate the App",
            "📱 Share CoLink"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Help & Support")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showUserGuide()
                    1 -> showFAQs()
                    2 -> contactSupport()
                    3 -> reportBug()
                    4 -> rateApp()
                    5 -> shareApp()
                }
            }
            .show()
    }

    private fun showUserGuide() {
        MaterialAlertDialogBuilder(this)
            .setTitle("CoLink User Guide")
            .setMessage("""
                Welcome to CoLink! Here's how to get started:
                
                1️⃣ Create Event Groups
                   - Tap "New Group" on the chat screen
                   - Set event details and invite via WhatsApp
                
                2️⃣ Pool Funds Securely
                   - Set a budget for your event
                   - Members contribute with biometric approval
                   - Track progress in real-time
                
                3️⃣ Stay Coordinated
                   - Chat with group members
                   - Share locations on event day
                   - Get weather alerts automatically
                
                4️⃣ Earn Rewards
                   - Participate in events to earn points
                   - Redeem rewards for perks
                
                For more help, visit: www.colink.com/guide
            """.trimIndent())
            .setPositiveButton("Got it!", null)
            .show()
    }

    private fun showFAQs() {
        val faqs = arrayOf(
            "How do I create an event group?",
            "How does the group fund work?",
            "Is my payment information secure?",
            "How do I invite friends?",
            "Can I track my location?",
            "How do I earn reward points?"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Frequently Asked Questions")
            .setItems(faqs) { _, which ->
                val answer = when (which) {
                    0 -> "Tap 'New Group' on the Chats screen, fill in event details, and invite members via WhatsApp."
                    1 -> "Set a budget when creating an event. Members can contribute funds that require biometric approval from the group."
                    2 -> "Yes! All transactions require biometric authentication and are encrypted. We comply with banking security standards."
                    3 -> "Use the 'Invite via WhatsApp' button when creating or in an event group to share an invite link."
                    4 -> "Yes, you can share your location with event groups. Go to Settings > Location Services to manage permissions."
                    5 -> "Earn points by creating events, participating in groups, and contributing to funds. Redeem in your profile."
                    else -> "For more information, contact support."
                }

                MaterialAlertDialogBuilder(this)
                    .setTitle(faqs[which])
                    .setMessage(answer)
                    .setPositiveButton("OK", null)
                    .show()
            }
            .show()
    }

    private fun contactSupport() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Contact Support")
            .setMessage("""
                Need help? Reach us at:
                
                📧 Email: support@colink.com
                📱 WhatsApp: +27 123 456 789
                🌐 Web: www.colink.com/support
                
                Support Hours:
                Monday - Friday: 8am - 6pm SAST
                Saturday: 9am - 2pm SAST
                
                We typically respond within 24 hours.
            """.trimIndent())
            .setPositiveButton("Email Support") { _, _ ->
                Toast.makeText(this, "Opening email...", Toast.LENGTH_SHORT).show()
                // TODO: Open email intent
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun reportBug() {
        Toast.makeText(this, "Bug Report - Opening form...", Toast.LENGTH_SHORT).show()
        // TODO: Open bug report form or email
    }

    private fun rateApp() {
        Toast.makeText(this, "Opening Play Store...", Toast.LENGTH_SHORT).show()
        // TODO: Open Play Store app page
    }

    private fun shareApp() {
        val shareText = """
            Join me on CoLink - The best app for organizing events with friends! 
            
            🎉 Create event groups
            💰 Pool funds securely
            📍 Share locations
            🌤️ Get weather alerts
            
            Download now: www.colink.com/download
        """.trimIndent()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(shareIntent, "Share CoLink"))
    }

    private fun showLanguageDialog() {
        val languages = arrayOf(
            "English",
            "Afrikaans",
            "Zulu",
            "Xhosa",
            "Sesotho"
        )

        val currentLanguage = prefs.getString("language", "English") ?: "English"
        val currentIndex = languages.indexOf(currentLanguage)

        MaterialAlertDialogBuilder(this)
            .setTitle("Choose Language")
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val selectedLanguage = languages[which]
                prefs.edit().putString("language", selectedLanguage).apply()
                currentLanguageText.text = selectedLanguage
                Toast.makeText(this, "Language changed to $selectedLanguage", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("About CoLink")
            .setMessage("""
                CoLink v1.0.0
                
                Get together, stay connected.
                
                CoLink is your all-in-one event coordination platform, making it easy to organize, fund, and enjoy gatherings with friends and family.
                
                Developed by: XDev Studios
                
                © 2025 CoLink. All rights reserved.
                
                Contact: info@colink.com
                Website: www.colink.com
                
                Built with ❤️ in South Africa
            """.trimIndent())
            .setPositiveButton("OK", null)
            .setNeutralButton("Terms of Service") { _, _ ->
                showTermsOfService()
            }
            .show()
    }

    private fun showTermsOfService() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Terms of Service")
            .setMessage("""
                CoLink Terms of Service
                
                By using CoLink, you agree to:
                
                1. Use the app responsibly and respectfully
                2. Provide accurate information
                3. Protect your account credentials
                4. Comply with all local laws
                5. Respect other users' privacy
                6. Not misuse the payment system
                7. Report any security issues
                
                We reserve the right to suspend accounts that violate these terms.
                
                Last updated: January 2025
                
                Full terms: www.colink.com/terms
            """.trimIndent())
            .setPositiveButton("I Agree", null)
            .show()
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Clear preferences
        prefs.edit().clear().apply()

        // TODO: Sign out from Firebase Auth
        // FirebaseAuth.getInstance().signOut()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to login screen (create later)
        // For now, just finish the activity
        finish()
    }
}