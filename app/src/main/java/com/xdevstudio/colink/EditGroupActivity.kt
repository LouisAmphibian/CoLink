package com.xdevstudio.colink

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EditGroupActivity : AppCompatActivity() {

    private lateinit var eventNameInput: TextInputEditText
    private lateinit var eventDescriptionInput: TextInputEditText
    private lateinit var eventDateInput: TextInputEditText
    private lateinit var eventTimeInput: TextInputEditText
    private lateinit var eventLocationInput: TextInputEditText
    private lateinit var budgetInput: TextInputEditText
    private lateinit var saveButton: Button
    private lateinit var addMembersButton: Button
    private lateinit var backButton: ImageButton

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var groupId: String

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_group)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        groupId = intent.getStringExtra("groupId") ?: ""

        initializeViews()
        setupClickListeners()
        loadGroupData()
    }

    private fun initializeViews() {
        eventNameInput = findViewById(R.id.eventNameInput)
        eventDescriptionInput = findViewById(R.id.eventDescriptionInput)
        eventDateInput = findViewById(R.id.eventDateInput)
        eventTimeInput = findViewById(R.id.eventTimeInput)
        eventLocationInput = findViewById(R.id.eventLocationInput)
        budgetInput = findViewById(R.id.budgetInput)
        saveButton = findViewById(R.id.saveButton)
        addMembersButton = findViewById(R.id.addMembersButton)
        backButton = findViewById(R.id.backButton)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener { finish() }
        saveButton.setOnClickListener { updateGroup() }
        addMembersButton.setOnClickListener { addMoreMembers() }

        setupDateAndTimePickers()
    }

    private fun setupDateAndTimePickers() {
        eventDateInput.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)

                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    eventDateInput.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
        }

        eventTimeInput.setOnClickListener {
            val timePicker = TimePickerDialog(
                this,
                { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)

                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    eventTimeInput.setText(timeFormat.format(calendar.time))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            )
            timePicker.show()
        }
    }

    private fun loadGroupData() {
        firestore.collection("groups")
            .document(groupId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val group = document.toObject(Group::class.java)
                    group?.let { populateFields(it) }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading group: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun populateFields(group: Group) {
        eventNameInput.setText(group.name)
        eventDescriptionInput.setText(group.description)

        calendar.time = group.date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        eventDateInput.setText(dateFormat.format(group.date))

        eventTimeInput.setText(group.time)
        eventLocationInput.setText(group.location)
        budgetInput.setText(if (group.budget > 0) group.budget.toString() else "")
    }

    private fun updateGroup() {
        val updates = hashMapOf<String, Any>(
            "name" to eventNameInput.text.toString().trim(),
            "description" to eventDescriptionInput.text.toString().trim(),
            "date" to calendar.time,
            "time" to eventTimeInput.text.toString().trim(),
            "location" to eventLocationInput.text.toString().trim(),
            "budget" to (budgetInput.text.toString().toDoubleOrNull() ?: 0.0),
            "updatedAt" to Date()
        )

        firestore.collection("groups")
            .document(groupId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Group updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update group: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addMoreMembers() {
        val intent = Intent(this, NewGroupActivity::class.java)
        intent.putExtra("groupId", groupId)
        intent.putExtra("addMembers", true)
        startActivity(intent)
    }
}