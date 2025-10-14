package com.xdevstudio.colink

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xdevstudio.colink.models.EventGroup
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class NewGroupActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var createButton: Button
    private lateinit var eventNameInput: TextInputEditText
    private lateinit var eventDescriptionInput: TextInputEditText
    private lateinit var eventDateInput: TextInputEditText
    private lateinit var eventTimeInput: TextInputEditText
    private lateinit var eventLocationInput: TextInputEditText
    private lateinit var budgetInput: TextInputEditText
    private lateinit var whatsappInviteButton: Button
    private lateinit var selectedMembersCount: TextView

    private var selectedDate: Date? = null
    private var selectedTime: String = ""
    private var selectedMembers = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)

        initializeViews()
        setupListeners()
        setupValidation()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        createButton = findViewById(R.id.createButton)
        eventNameInput = findViewById(R.id.eventNameInput)
        eventDescriptionInput = findViewById(R.id.eventDescriptionInput)
        eventDateInput = findViewById(R.id.eventDateInput)
        eventTimeInput = findViewById(R.id.eventTimeInput)
        eventLocationInput = findViewById(R.id.eventLocationInput)
        budgetInput = findViewById(R.id.budgetInput)
        whatsappInviteButton = findViewById(R.id.whatsappInviteButton)
        selectedMembersCount = findViewById(R.id.selectedMembersCount)
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }

        createButton.setOnClickListener {
            createEventGroup()
        }

        eventDateInput.setOnClickListener {
            showDatePicker()
        }

        eventTimeInput.setOnClickListener {
            showTimePicker()
        }

        whatsappInviteButton.setOnClickListener {
            openWhatsAppInvite()
        }
    }

    private fun setupValidation() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        eventNameInput.addTextChangedListener(textWatcher)
        eventDateInput.addTextChangedListener(textWatcher)
        eventTimeInput.addTextChangedListener(textWatcher)
        eventLocationInput.addTextChangedListener(textWatcher)
    }

    private fun validateForm() {
        val isValid = !eventNameInput.text.isNullOrEmpty() &&
                !eventDateInput.text.isNullOrEmpty() &&
                !eventTimeInput.text.isNullOrEmpty() &&
                !eventLocationInput.text.isNullOrEmpty()

        createButton.isEnabled = isValid
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = Calendar.getInstance()
                date.set(selectedYear, selectedMonth, selectedDay)
                selectedDate = date.time

                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                eventDateInput.setText(dateFormat.format(selectedDate))
            },
            year,
            month,
            day
        )

        // Don't allow selecting past dates
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val time = Calendar.getInstance()
                time.set(Calendar.HOUR_OF_DAY, selectedHour)
                time.set(Calendar.MINUTE, selectedMinute)

                selectedTime = timeFormat.format(time.time)
                eventTimeInput.setText(selectedTime)
            },
            hour,
            minute,
            false // 12-hour format
        )

        timePickerDialog.show()
    }

    private fun openWhatsAppInvite() {
        // Create WhatsApp invite message
        val eventName = eventNameInput.text.toString()
        val message = """
            🎉 You're invited to: $eventName
            
            Join our event group on CoLink to stay connected and coordinate!
            
            Download CoLink: [App Link]
        """.trimIndent()

        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(message)}")
            startActivity(intent)

            // Simulate member selection (in real app, this would come from WhatsApp response)
            selectedMembers += 1
            selectedMembersCount.text = "$selectedMembers members selected"
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "WhatsApp not installed. Please install WhatsApp to invite members.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun createEventGroup() {
        val name = eventNameInput.text.toString()
        val description = eventDescriptionInput.text.toString()
        val location = eventLocationInput.text.toString()
        val budgetText = budgetInput.text.toString()
        val budget = if (budgetText.isNotEmpty()) budgetText.toDouble() else 0.0

        // Create EventGroup object
        val eventGroup = EventGroup(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            eventDate = selectedDate,
            eventTime = selectedTime,
            location = location,
            budget = budget,
            creatorId = getCurrentUserId(), // TODO: Get from Firebase Auth
            hasFundingActive = budget > 0
        )

        // TODO: Save to Firebase Firestore
        saveEventGroupToFirebase(eventGroup)
    }

    private fun saveEventGroupToFirebase(eventGroup: EventGroup) {
        // TODO: Implement Firebase Firestore save
        // For now, just show success and return

        Toast.makeText(this, "Event group created successfully!", Toast.LENGTH_SHORT).show()

        // Return to ChatActivity
        setResult(RESULT_OK)
        finish()
    }

    private fun getCurrentUserId(): String {
        // TODO: Get from Firebase Authentication
        return "user_${UUID.randomUUID()}"
    }
}

// Additional utility class for formatting
object DateTimeFormatter {
    fun formatEventDateTime(date: Date?, time: String): String {
        return if (date != null) {
            val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
            "${dateFormat.format(date)} at $time"
        } else {
            "Date not set"
        }
    }

    fun formatCurrency(amount: Double): String {
        return "R %.2f".format(amount)
    }

    fun getRelativeTime(date: Date): String {
        val now = System.currentTimeMillis()
        val diff = now - date.time

        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            diff < 604800000 -> "${diff / 86400000}d ago"
            else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
        }
    }
}