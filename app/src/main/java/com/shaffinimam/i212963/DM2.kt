package com.shaffinimam.i212963

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DM2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dm2)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val name = intent.getStringExtra("name")
        toolbar.title=name;
        val cal= findViewById<ImageButton>(R.id.callpers)
        cal.setOnClickListener{
            val intent = Intent(this,Call::class.java)
            intent.putExtra("name",name)
            startActivity(intent)
        }


    }
    override fun onSupportNavigateUp(): Boolean {
        finish() // Close current activity
        return true
    }
}