package com.shaffinimam.i212963

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        val logText = findViewById<TextView>(R.id.logi)

        // Set onClickListener
        logText.setOnClickListener {
            val intent = Intent(this, Login::class.java) // Replace with your class name
            startActivity(intent)
        }
    }
}