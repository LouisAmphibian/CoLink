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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileCreationActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var addPhotoButton: ImageButton
    private lateinit var nameEditText: EditText
    private lateinit var nextButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var progressIndicator: ProgressBar


    private var profileImageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage


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

        //Initialize Firebase services
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), 100)
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        }
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
                saveProfileToFirebase(name)
            }
        }
    }

    /*
    private fun simulateProfileSave(name: String) {
        //saving profile
        val currentUser = auth.currentUser

        //check if user is authenticated or not
        if (currentUser == null){
            showToast("User not authenticated")
            setLoadingState(false)
            return
        }

        val userId = currentUser.uid
        val phoneNumber = currentUser.phoneNumber ?: ""

        //if there's a profile image, upload it first
        if (profileImageUri != null){
            uploadProfileImage(userId, name, phoneNumber)
        }else{
            //Save profile without image
            saveUserProfileToFirestore(userId, name, phoneNumber, null)
        }

        Handler(mainLooper).postDelayed({
            setLoadingState(false)
            showToast("Profile saved successfully!")
            navigateToChatActivity()
        }, 1500)
    }
*/

    private fun saveProfileToFirebase(name: String) {
        val currentUser = auth.currentUser

        // Check if user is authenticated or not
        if (currentUser == null) {
            showToast("User not authenticated")
            setLoadingState(false)
            // Redirect to verification
            navigateToVerificationActivity()
            return
        }

        val userId = currentUser.uid
        val phoneNumber = currentUser.phoneNumber ?: ""

        // If there's a profile image, upload it first
        if (profileImageUri != null) {
            uploadProfileImage(userId, name, phoneNumber)
        } else {
            // Save profile without image
            saveUserProfileToFirestore(userId, name, phoneNumber, null)
        }
    }


    private fun uploadProfileImage(userId : String, name : String, phoneNumber: String){
        val imageRef = storage.reference.child("profile_images/$userId.jpg")

        imageRef.putFile(profileImageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                //Get the download URL after successful upload
                imageRef.downloadUrl.addOnSuccessListener { downloadUri->
                    saveUserProfileToFirestore(userId, name, phoneNumber, downloadUri.toString())
                }
            }
            .addOnFailureListener { exception ->
                setLoadingState(false)
                showToast("Failed to upload image: ${exception.message}")
            }
    }

    private fun saveUserProfileToFirestore(userId: String, name: String, phoneNumber: String, profileImageUrl: String?){
        val userProfile = User(
            userId = userId,
            name = name,
            phoneNumber = phoneNumber,
            profileImageUrl = profileImageUrl,
            createdAt = com.google.firebase.Timestamp.now(),
            updatedAt = com.google.firebase.Timestamp.now()
        )

        //logging to debug co-link
        println("🔥🔥Attempting to save user profile:  $userProfile")

        firestore.collection("users")
            .document(userId)
            .set(userProfile)
            .addOnSuccessListener {
                println("✅Profile saved successfully")
                setLoadingState(false)
                showToast("✅Profile saved successfully")
                navigateToChatActivity()
            }
            .addOnFailureListener { exception ->
                println("❌Failed to save profile ${exception.message}") //Debug
                setLoadingState(false)
                showToast("❌Failed to save profile ${exception.message}")
            }
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

    private fun navigateToVerificationActivity() {
        val intent = Intent(this, VerificationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    private fun navigateToChatActivity() {
        // Instead of going directly to ChatActivity, go to MainActivity which will show splash then chat
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
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