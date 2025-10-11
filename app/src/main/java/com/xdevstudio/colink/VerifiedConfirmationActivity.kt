package com.xdevstudio.colink

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider

class VerifiedConfirmationActivity : AppCompatActivity() {

    // 1️ Firebase authentication variables
    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verifiedconfirmation)

        // 2️ Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 3️ Retrieve data passed from VerificationActivity (the number + verification ID)
        verificationId = intent.getStringExtra("verificationId") ?: ""
        phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        // 4️ Get references to the 6 OTP input boxes
        val otpInputs = listOf<EditText>(
            findViewById(R.id.otp1),
            findViewById(R.id.otp2),
            findViewById(R.id.otp3),
            findViewById(R.id.otp4),
            findViewById(R.id.otp5),
            findViewById(R.id.otp6)
        )

        // 5️ Create a “Verify” button programmatically
        val verifyButton = Button(this)
        verifyButton.text = "Verify"

        // 6️ When the button is clicked, collect the digits entered by the user
        verifyButton.setOnClickListener {
            val otpCode = otpInputs.joinToString("") { it.text.toString() }

            // 7️ Make sure all 6 digits are filled in
            if (otpCode.length == 6) {
                val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
                signInWithCredential(credential)  // Go verify with Firebase
            } else {
                Toast.makeText(this, "Enter full 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 8️ Verify the OTP code with Firebase
    private fun signInWithCredential(credential: com.google.firebase.auth.PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Verification successful!", Toast.LENGTH_SHORT).show()
                saveNumberToSQLite(phoneNumber)  // Save the verified number locally
            } else {
                Toast.makeText(this, "Verification failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 9️ Save verified phone number to SQLite database
    private fun saveNumberToSQLite(phone: String) {
        val db: SQLiteDatabase = openOrCreateDatabase("CoLinkDB", MODE_PRIVATE, null)
        db.execSQL("CREATE TABLE IF NOT EXISTS users(phone TEXT)")
        val values = ContentValues().apply { put("phone", phone) }
        db.insert("users", null, values)
        db.close()
    }
}
