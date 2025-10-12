package com.xdevstudio.colink

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.InputFilter
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.Locale
import java.util.concurrent.TimeUnit

class VerificationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        val spinner = findViewById<Spinner>(R.id.countrySpinner)
        val codeField = findViewById<EditText>(R.id.countryCode)
        val phoneNumber = findViewById<EditText>(R.id.phoneNumber)
        val nextButton = findViewById<Button>(R.id.nextButton)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 1️ List of countries and dial codes
        val countries = listOf(
            "South Africa (+27)" to "+27",
            "Nigeria (+234)" to "+234",
            "United States (+1)" to "+1",
            "United Kingdom (+44)" to "+44",
            "India (+91)" to "+91",
            "France (+33)" to "+33",
            "Portugal (+351)" to "+351"
        )

        // 2️ Spinner adapter
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            countries.map { it.first }
        )
        spinner.adapter = adapter

        // 3️ Phone length rules per country
        val phoneLengths = mapOf(
            "+27" to 9,   // South Africa
            "+234" to 10, // Nigeria
            "+1" to 10,   // USA
            "+44" to 10,  // UK
            "+91" to 10,  // India
            "+33" to 9,   // France
            "+351" to 9   // Portugal
        )

        // 4️ Detect user’s current country automatically
        val defaultCountryIso = getUserCountryCode().uppercase(Locale.ROOT)
        val defaultIndex = countries.indexOfFirst { it.first.contains(defaultCountryIso, ignoreCase = true) }

        if (defaultIndex >= 0) {
            spinner.setSelection(defaultIndex)
            codeField.setText(countries[defaultIndex].second)
        } else {
            spinner.setSelection(0)
            codeField.setText(countries[0].second)
        }

        // 5️  Update code + phone length on country change
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val code = countries[position].second
                codeField.setText(code)
                phoneNumber.text.clear()
                val maxLen = phoneLengths[code] ?: 10
                phoneNumber.filters = arrayOf(InputFilter.LengthFilter(maxLen))
                phoneNumber.hint = "Enter $maxLen-digit number"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 6️ Handle "Next" button click
        nextButton.setOnClickListener {
            val phone = phoneNumber.text.toString().trim()
            val code = codeField.text.toString()
            val fullNumber = "$code$phone"

            if (phone.isBlank()) {
                Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (phone.length < (phoneLengths[code] ?: 9)) {
                Toast.makeText(this, "Phone number too short for $code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendVerificationCode(fullNumber)
        }
    }

    // 7️ Send verification code via Firebase
    private fun sendVerificationCode(phone: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-verification (may happen instantly on some devices)
                    Toast.makeText(applicationContext, "Auto verification completed", Toast.LENGTH_SHORT).show()
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(verificationIdParam: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = verificationIdParam
                    Toast.makeText(applicationContext, "OTP sent to $phone", Toast.LENGTH_SHORT).show()

                    // Move to OTP screen
                    val intent = Intent(this@VerificationActivity, VerifiedConfirmationActivity::class.java)
                    intent.putExtra("verificationId", verificationId)
                    intent.putExtra("phoneNumber", phone)
                    startActivity(intent)

                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // 8️ Auto-detect device country
    private fun getUserCountryCode(): String {
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simCountry = tm.simCountryIso
        val networkCountry = tm.networkCountryIso
        val localeCountry = Locale.getDefault().country

        return when {
            !simCountry.isNullOrEmpty() -> simCountry
            !networkCountry.isNullOrEmpty() -> networkCountry
            else -> localeCountry
        }
    }
}

/*Testing the OTP
Test phone number: +27600000000
OTP: 123456
 */