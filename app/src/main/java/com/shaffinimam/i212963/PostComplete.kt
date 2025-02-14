package com.shaffinimam.i212963

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PostComplete : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_post_complete)
        val buttonC = findViewById<ImageButton>(R.id.closebutt)
        buttonC.setOnClickListener{
            val intent = Intent(this, Navigation::class.java)
            startActivity(intent)
        }
    }
}