package com.shaffinimam.i212963

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
class Register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        val logText = findViewById<TextView>(R.id.logi)

        // Set onClickListener
        logText.setOnClickListener {
            val intent = Intent(this, Login::class.java) // Replace with your class name
            startActivity(intent)
        }

        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.passw)
        val phoneEditText = findViewById<EditText>(R.id.phoneno)
        val usrnamEditText = findViewById<EditText>(R.id.usr)
        val registerButton = findViewById<Button>(R.id.regs)
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val usrn = usrnamEditText.text.toString().trim()
            val phnen = phoneEditText.text.toString().trim()
            if (email.isNotEmpty() && password.length >= 6 && usrn.isNotEmpty() && phnen.isNotEmpty()) {
                registerUser(email, usrn,password,phnen)
            } else {
                Toast.makeText(this, "Enter valid email and password (min 6 chars)", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun registerUser(email:String,username: String,password:String,phone:String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                    val uid = user?.uid
                    saveToRDB(email,username,phone,uid.toString())

                } else {
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
    data class User(val id: String, val username: String, val email: String, val phone: String,val followers:String,val following:String)
    private fun saveToRDB(email:String,username: String,phone:String,id:String){
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val usersRef: DatabaseReference = database.getReference("users/") // Root node "users"

        // Create a user object
        val user = User(id, username, email,phone,"0","0")

        // Save user data under the userId node
        usersRef.child(id).setValue(user)
            .addOnSuccessListener {
                val intent = Intent(this, EditProfile::class.java) // Replace with your class name
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                println("Failed to write data: ${e.message}")
            }
    }

}