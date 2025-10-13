package com.xdevstudio.colink

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider

class VerifiedConfirmationActivity : AppCompatActivity() {

    // Firebase authentication variables
    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verifiedconfirmation)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Retrieve data passed from VerificationActivity (the number + verification ID)
        verificationId = intent.getStringExtra("verificationId") ?: ""
        phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        // Update info text to show the phone number being verified
        val infoText = findViewById<TextView>(R.id.infoText)
        infoText.text = "Enter the 6-digit code sent to $phoneNumber"

        // Get references to the 6 OTP input boxes
        val otpInputs = listOf<EditText>(
            findViewById(R.id.otp1),
            findViewById(R.id.otp2),
            findViewById(R.id.otp3),
            findViewById(R.id.otp4),
            findViewById(R.id.otp5),
            findViewById(R.id.otp6)
        )

        // Get the Verify button from XML (not creating programmatically)
        val verifyButton = findViewById<Button>(R.id.verifyButton)
        val resendText = findViewById<TextView>(R.id.resendText)
        val timerText = findViewById<TextView>(R.id.timerText)

        // Setup OTP auto-advance functionality
        setupOtpAutoAdvance(otpInputs, verifyButton)

        // When the Verify button is clicked, collect the digits entered by the user
        verifyButton.setOnClickListener {
            val otpCode = otpInputs.joinToString("") { it.text.toString() }

            // Make sure all 6 digits are filled in
            if (otpCode.length == 6) {
                // Handle test verification (for Firebase test numbers)
                if (verificationId == "test_verification_id" && otpCode == "123456") {
                    Toast.makeText(this, "Test verification successful!", Toast.LENGTH_SHORT).show()
                    saveNumberToSQLite(phoneNumber)
                    // TODO: Navigate to main activity
                } else {
                    val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
                    signInWithCredential(credential)  // Go verify with Firebase
                }
            } else {
                Toast.makeText(this, "Enter full 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle resend code functionality
        resendText.setOnClickListener {
            // TODO: Implement resend code logic
            Toast.makeText(this, "Code resent to $phoneNumber", Toast.LENGTH_SHORT).show()

        //Might want to restart the timer here
        }

        // Start countdown timer (example implementation)
        startResendTimer(timerText, resendText)
    }

    // Setup OTP auto-advance between fields
    private fun setupOtpAutoAdvance(otpInputs: List<EditText>, verifyButton: Button) {
        for (i in otpInputs.indices) {
            otpInputs[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        // Move to next field if available
                        if (i < otpInputs.size - 1) {
                            otpInputs[i + 1].requestFocus()
                        } else {
                            // Last field filled, optionally auto-submit
                            verifyButton.performClick()
                        }
                    } else if (s?.length == 0 && i > 0) {

                        // Move to previous field when backspace is pressed on empty field
                        otpInputs[i - 1].requestFocus()
                    }
                }
            })
        }
    }

    // Start countdown timer for resend functionality
    private fun startResendTimer(timerText: TextView, resendText: TextView) {
        var timeLeft = 60 // 60 seconds

        val timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft--
                val minutes = timeLeft / 60
                val seconds = timeLeft % 60
                timerText.text = "You may request a new code in ${String.format("%d:%02d", minutes, seconds)}"

                // Disable resend text while timer is running
                resendText.isEnabled = false
                resendText.setTextColor(getColor(android.R.color.darker_gray))
            }

            override fun onFinish() {
                timerText.text = "Didn't receive the code?"
                resendText.isEnabled = true
                resendText.setTextColor(getColor(R.color.colink_primary2))
            }
        }

        timer.start()
    }

    // Verify the OTP code with Firebase
    private fun signInWithCredential(credential: com.google.firebase.auth.PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Verification successful!", Toast.LENGTH_SHORT).show()
                saveNumberToSQLite(phoneNumber)  // Save the verified number locally

                // TODO: Navigate to main activity after successful verification
                val intent = Intent(this,ProfileCreationActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Verification failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Save verified phone number to SQLite database
    private fun saveNumberToSQLite(phone: String) {
        val db: SQLiteDatabase = openOrCreateDatabase("CoLinkDB", MODE_PRIVATE, null)
        db.execSQL("CREATE TABLE IF NOT EXISTS users(phone TEXT)")
        val values = ContentValues().apply { put("phone", phone) }
        db.insert("users", null, values)
        db.close()

        // TODO: Add more user data or handle this differently
    }
}