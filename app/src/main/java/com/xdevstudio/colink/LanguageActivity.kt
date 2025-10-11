package com.xdevstudio.colink

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class LanguageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_language)

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupLanguages)
        val nextButton = findViewById<ImageButton>(R.id.btn_next)

        nextButton.setOnClickListener {
            //Get selected radio button ID
            val selectedId = radioGroup.checkedRadioButtonId
            if(selectedId == -1){
                Toast.makeText(this,"Please select a language", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Find selected radio button
            val selectedButton = findViewById<RadioButton>(selectedId)
            val selectedLanguage = selectedButton.text.toString()

            //Applyin locale using mordern Andriod API
            val langCode = mapLanguageToCode(selectedLanguage)

            //Apply selected language
            applyAppLocale(langCode)
            saveLanguagePreference(langCode)

            //Move to next page
            val intent = Intent(this, VerificationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun  mapLanguageToCode(languageName: String): String{
            return when (languageName.lowercase(Locale.ROOT)){
            "english" -> "en"
            "isizulu" -> "zu"
            "isixhosa" -> "xh"
            "afrikaans" -> "af"
            "sesotho" -> "st"
            "setswana" -> "tn"
            "xitsonga" -> "ts"
            "french" -> "fr"
            "portuguese" -> "pt"
            else -> "en"
        }
    }

    private fun applyAppLocale(languageCode: String){
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)

        //works on andriod 13+
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocale)
    }

    private fun saveLanguagePreference(languageCode: String){
        val prefs: SharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        prefs.edit().putString("language", languageCode).apply()
    }
}