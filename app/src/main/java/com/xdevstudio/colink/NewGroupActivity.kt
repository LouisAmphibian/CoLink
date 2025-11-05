package com.xdevstudio.colink

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class NewGroupActivity : AppCompatActivity() {

    private lateinit var eventNameInput: TextInputEditText
    private lateinit var eventDescriptionInput: TextInputEditText
    private lateinit var eventDateInput: TextInputEditText
    private lateinit var eventTimeInput: TextInputEditText
    private lateinit var eventLocationInput: TextInputEditText
    private lateinit var budgetInput: TextInputEditText
    private lateinit var createButton: Button
    private lateinit var whatsappInviteButton: Button
    private lateinit var selectedMembersCount: TextView
    private lateinit var backButton: ImageButton

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val selectedContacts = mutableListOf<Contact>()
    private val calendar = Calendar.getInstance()

    companion object {
        const val CONTACT_PICKER_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initializeViews()
        setupClickListeners()
        setupDateAndTimePickers()
    }

    private fun initializeViews() {
        eventNameInput = findViewById(R.id.eventNameInput)
        eventDescriptionInput = findViewById(R.id.eventDescriptionInput)
        eventDateInput = findViewById(R.id.eventDateInput)
        eventTimeInput = findViewById(R.id.eventTimeInput)
        eventLocationInput = findViewById(R.id.eventLocationInput)
        budgetInput = findViewById(R.id.budgetInput)
        createButton = findViewById(R.id.createButton)
        whatsappInviteButton = findViewById(R.id.whatsappInviteButton)
        selectedMembersCount = findViewById(R.id.selectedMembersCount)
        backButton = findViewById(R.id.backButton)

        updateSelectedMembersCount()
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener { finish() }

        createButton.setOnClickListener { createGroup() }
        whatsappInviteButton.setOnClickListener { openContactPicker() }

        // Enable create button when event name is entered
        eventNameInput.setOnKeyListener { _, _, _ ->
            validateForm()
            false
        }
    }

    private fun setupDateAndTimePickers() {
        // Date Picker
        eventDateInput.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)

                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    eventDateInput.setText(dateFormat.format(calendar.time))
                    validateForm()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
        }

        // Time Picker
        eventTimeInput.setOnClickListener {
            val timePicker = TimePickerDialog(
                this,
                { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)

                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    eventTimeInput.setText(timeFormat.format(calendar.time))
                    validateForm()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            )
            timePicker.show()
        }
    }

    private fun openContactPicker() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, CONTACT_PICKER_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CONTACT_PICKER_REQUEST && resultCode == RESULT_OK) {
            data?.data?.let { contactUri ->
                val contact = getContactDetails(contactUri)
                contact?.let {
                    showContactSelectionDialog(it)
                }
            }
        }
    }

    private fun getContactDetails(contactUri: Uri): Contact? {
        return try {
            val projection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
            )

            val cursor = contentResolver.query(contactUri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val idColumnIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                    val nameColumnIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val hasPhoneColumnIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                    // Check if columns exist
                    if (idColumnIndex == -1 || nameColumnIndex == -1 || hasPhoneColumnIndex == -1) {
                        return null
                    }

                    val id = it.getString(idColumnIndex)
                    val name = it.getString(nameColumnIndex) ?: "Unknown"

                    // Get phone number
                    var phoneNumber = ""
                    if (it.getInt(hasPhoneColumnIndex) > 0) {
                        val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            phoneProjection,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )
                        phoneCursor?.use { pc ->
                            val numberColumnIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (numberColumnIndex != -1 && pc.moveToFirst()) {
                                phoneNumber = pc.getString(numberColumnIndex) ?: ""
                            }
                        }
                    }

                    Contact(
                        id = id,
                        name = name,
                        phoneNumber = phoneNumber,
                        normalized = normalizePhoneNumber(phoneNumber)
                    )
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showContactSelectionDialog(contact: Contact) {
        // Check if contact is already on CoLink
        checkIfContactIsOnCoLink(contact) { isOnCoLink, userId ->
            contact.isOnCoLink = isOnCoLink
            contact.userId = userId

            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Add ${contact.name}")
                .setMessage(
                    if (isOnCoLink) {
                        "${contact.name} is on CoLink. Add them to your group?"
                    } else {
                        "${contact.name} is not on CoLink. Invite them via WhatsApp?"
                    }
                )
                .setPositiveButton(if (isOnCoLink) "Add" else "Invite") { _, _ ->
                    if (isOnCoLink) {
                        // Add to selected contacts
                        if (!selectedContacts.any { it.id == contact.id }) {
                            selectedContacts.add(contact)
                            updateSelectedMembersCount()
                            Toast.makeText(this, "Added ${contact.name}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Send WhatsApp invite
                        sendWhatsAppInvite(contact)
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()
            dialog.show()
        }
    }

    private fun checkIfContactIsOnCoLink(contact: Contact, callback: (Boolean, String?) -> Unit) {
        val normalizedPhone = normalizePhoneNumber(contact.phoneNumber)

        if (normalizedPhone.isEmpty()) {
            callback(false, null)
            return
        }

        firestore.collection("users")
            .whereEqualTo("phoneNumber", normalizedPhone)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    callback(true, document.id)
                } else {
                    callback(false, null)
                }
            }
            .addOnFailureListener {
                callback(false, null)
            }
    }

    private fun sendWhatsAppInvite(contact: Contact) {
        // Filter phone number to get only digits
        val phoneDigits = contact.phoneNumber.filter { it.isDigit() }
        if (phoneDigits.isEmpty()) {
            Toast.makeText(this, "No valid phone number found for ${contact.name}", Toast.LENGTH_SHORT).show()
            return
        }

        val message = "Hi ${contact.name}! Join me on CoLink to plan events together. Download the app here: [App Store/Play Store Link]"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$phoneDigits/?text=${Uri.encode(message)}")
            setPackage("com.whatsapp")
        }

        try {
            startActivity(intent)
            Toast.makeText(this, "Opening WhatsApp to invite ${contact.name}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            // Fallback: share via other means
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            startActivity(Intent.createChooser(shareIntent, "Invite via"))
        }
    }

    private fun createGroup() {
        if (!validateForm()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedContacts.size < 1) { // At least 1 other person + creator = 2 total
            Toast.makeText(this, "Please add at least one person to the group", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Create group object
        val groupId = firestore.collection("groups").document().id
        val group = Group(
            id = groupId,
            name = eventNameInput.text.toString().trim(),
            description = eventDescriptionInput.text.toString().trim(),
            date = calendar.time,
            location = eventLocationInput.text.toString().trim(),
            budget = budgetInput.text.toString().toDoubleOrNull() ?: 0.0,
            createdBy = currentUser.uid,
            status = "pending", // pending until 2+ people accept
            members = mutableListOf(GroupMember(currentUser.uid, "admin", accepted = true)),
            invitedMembers = selectedContacts.map { contact ->
                InvitedMember(
                    phoneNumber = normalizePhoneNumber(contact.phoneNumber),
                    name = contact.name,
                    isOnCoLink = contact.isOnCoLink,
                    userId = contact.userId,
                    invitedAt = Date()
                )
            }.toMutableList(),
            createdAt = Date(),
            updatedAt = Date()
        )

        // Save to Firestore
        firestore.collection("groups")
            .document(groupId)
            .set(group)
            .addOnSuccessListener {
                Toast.makeText(this, "Group created successfully!", Toast.LENGTH_SHORT).show()

                // Send invites to CoLink users immediately
                sendCoLinkInvites(group)

                // Navigate back to chats
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to create group: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendCoLinkInvites(group: Group) {
        val coLinkMembers = group.invitedMembers.filter { it.isOnCoLink && it.userId != null }

        coLinkMembers.forEach { invitedMember ->
            // Send notification to the user
            val notification = mapOf(
                "type" to "group_invite",
                "groupId" to group.id,
                "groupName" to group.name,
                "invitedBy" to group.createdBy,
                "timestamp" to Date()
            )

            firestore.collection("users")
                .document(invitedMember.userId!!)
                .collection("notifications")
                .add(notification)
        }
    }

    private fun validateForm(): Boolean {
        val isValid = eventNameInput.text?.isNotEmpty() == true &&
                eventDateInput.text?.isNotEmpty() == true &&
                eventTimeInput.text?.isNotEmpty() == true

        createButton.isEnabled = isValid
        return isValid
    }

    private fun updateSelectedMembersCount() {
        selectedMembersCount.text = "${selectedContacts.size} members selected"
    }

    private fun normalizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace("[^0-9+]".toRegex(), "")
    }
}