package com.xdevstudio.colink

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible

class ProfileCreationActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var addPhotoButton: ImageButton
    private lateinit var nameEditText: EditText
    private lateinit var nextButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var progressIndicator: ProgressBar

    private var profileImageUri: Uri? = null

    // Modern Activity Result API - replaces startActivityForResult
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profileImageUri = it
            profileImage.setImageURI(it)
        }
    }

    // Modern back press handling
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleBackNavigation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_creation)

        initializeViews()
        setupClickListeners()
        setupBackPressHandler()
    }

    private fun initializeViews() {
        profileImage = findViewById(R.id.profileImage)
        addPhotoButton = findViewById(R.id.addPhotoButton)
        nameEditText = findViewById(R.id.nameEditText)
        nextButton = findViewById(R.id.nextButton)
        backButton = findViewById(R.id.backButton)

        // Add progress indicator programmatically if not in XML
        progressIndicator = ProgressBar(this).apply {
            isIndeterminate = true
            isVisible = false
        }
    }

    private fun setupClickListeners() {
        addPhotoButton.setOnClickListener { openImagePicker() }
        profileImage.setOnClickListener { openImagePicker() }
        nextButton.setOnClickListener { saveProfileData() }
        backButton.setOnClickListener { handleBackNavigation() }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun saveProfileData() {
        val name = nameEditText.text.toString().trim()

        when {
            name.isEmpty() -> {
                showToast("Please enter your name")
                return
            }
            name.length < 2 -> {
                showToast("Name must be at least 2 characters")
                return
            }
            else -> {
                setLoadingState(true)
                // Simulate API call or database operation
                simulateProfileSave(name)
            }
        }
    }

    private fun simulateProfileSave(name: String) {
        // Replace with actual Firebase/API call
        Handler(mainLooper).postDelayed({
            setLoadingState(false)
            showToast("Profile saved successfully!")
            navigateToMainActivity()
        }, 1500)
    }

    private fun setLoadingState(isLoading: Boolean) {
        progressIndicator.isVisible = isLoading
        nextButton.isEnabled = !isLoading
        nextButton.text = if (isLoading) "Saving..." else "Next"

        // Disable interactions during loading
        nameEditText.isEnabled = !isLoading
        addPhotoButton.isEnabled = !isLoading
        profileImage.isEnabled = !isLoading
    }

    private fun handleBackNavigation() {
        if (!progressIndicator.isVisible) {
            finish()
        }
        // If loading, back press is ignored - user must wait
    }

    private fun navigateToMainActivity() {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Clean up resources if needed
    override fun onDestroy() {
        onBackPressedCallback.remove()
        super.onDestroy()
    }
}