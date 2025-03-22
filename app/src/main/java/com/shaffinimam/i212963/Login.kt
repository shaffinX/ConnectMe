package com.shaffinimam.i212963

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.FirebaseDatabase

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.usr)
        val passwordEditText = findViewById<EditText>(R.id.passw)
        val loginButton = findViewById<Button>(R.id.log)
        val registerText = findViewById<TextView>(R.id.regt)
        val forgotPasswordText = findViewById<TextView>(R.id.forgp)

        // Login button click listener
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Validate input fields
            if (email.isEmpty()) {
                emailEditText.error = "Email is required"
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordEditText.error = "Password is required"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }

            // Show progress (optional - add a progress bar in your layout)
            // progressBar.visibility = View.VISIBLE

            // Authenticate with Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->


                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        val userId = auth.currentUser?.uid // Get user UID

                        if (userId != null) {
                            val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

                            databaseRef.get().addOnSuccessListener { snapshot ->
                                if (snapshot.exists()) {
                                    val username = snapshot.child("username").value.toString()
                                    SharedPrefManager.saveUsername(this, username) // Save username in SharedPreferences
                                }
                            }
                        }
                        val intent = Intent(this, Navigation::class.java)
                        startActivity(intent)
                        finish() // Close the login activity
                    } else {
                        // If sign in fails, display a message to the user
                        when (task.exception) {
                            is FirebaseAuthInvalidUserException -> {
                                Toast.makeText(this, "User not found. Please check your email or register.",
                                    Toast.LENGTH_SHORT).show()
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                Toast.makeText(this, "Invalid credentials. Please try again.",
                                    Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this, "Authentication failed: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }

        // Register text click listener
        registerText.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        // Forgot password text click listener
        forgotPasswordText.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to send reset email: ${task.exception?.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in and update UI accordingly
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already signed in, redirect to main activity
            val intent = Intent(this, Navigation::class.java)
            startActivity(intent)
            finish()
        }
    }
}