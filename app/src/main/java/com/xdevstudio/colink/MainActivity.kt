package com.xdevstudio.colink

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //Delay for 7 seconds before moving to next screen
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LanguageActivity::class.java)
            startActivity(intent)
            finish()//close splash so user can't return
        }, 7000) //7000 milliseconds = 7 seconds
    }
}