package com.shaffinimam.i212963

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        //forceDarkStatusBarIcons()
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }
//    private fun forceDarkStatusBarIcons() {
//        // Set status bar background to a light color (e.g., white)
//        window.statusBarColor = Color.WHITE
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//
//        // Force dark icons in the status bar
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//        }
//
//        // For Android 11 (API 30) and above, use WindowInsetsController
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            WindowCompat.getInsetsController(window, window.decorView).apply {
//                isAppearanceLightStatusBars = true
//            }
//        }
//    }
}