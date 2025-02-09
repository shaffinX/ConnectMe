package com.shaffinimam.i212963

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class Login : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val regText = findViewById<TextView>(R.id.regt)

        // Set onClickListener
        regText.setOnClickListener {
            val intent = Intent(this, Register::class.java) // Replace with your class name
            startActivity(intent)
        }
    }
}