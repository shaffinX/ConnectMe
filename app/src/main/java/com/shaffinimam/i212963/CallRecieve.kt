package com.shaffinimam.i212963

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CallRecieve : AppCompatActivity() {
    private lateinit var callRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_recieve)

        // Get data passed from intent
        val currentUid = intent.getStringExtra("uid") ?:
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val channelName = intent.getStringExtra("channelName") ?: ""

        // Get the caller's information from Firebase
        val callRef = FirebaseDatabase.getInstance().getReference("calls").child(currentUid)
        callRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Get caller data from the database, not from intent
                    val callerUid = snapshot.child("callerUid").getValue(String::class.java) ?: ""
                    val callerName = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
                    val callerPic = snapshot.child("picture").getValue(String::class.java) ?: ""

                    // Set up UI with caller's information
                    val nameText = findViewById<TextView>(R.id.incoming_name)
                    nameText.text = "Incoming Call from $callerName"

                    // Set caller profile pic if available
                    try {
                        if (callerPic.isNotEmpty()) {
                            val profileImage = findViewById<ImageView>(R.id.profile_img)
                            if (profileImage != null) {
                                val decodedBytes = Base64.decode(callerPic, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                profileImage.setImageBitmap(bitmap)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // Set up accept button
                    val acceptBtn = findViewById<ImageButton>(R.id.accept_call)
                    acceptBtn.setOnClickListener {
                        // Start call activity with caller's information
                        val intent = Intent(this@CallRecieve, Call::class.java)
                        intent.putExtra("channelName", channelName)
                        intent.putExtra("uid", currentUid)
                        intent.putExtra("isCaller", false)
                        intent.putExtra("pic", callerPic)
                        intent.putExtra("name", callerName)
                        startActivity(intent)
                        finish()
                    }

                    // Reject call button
                    val rejectBtn = findViewById<ImageButton>(R.id.reject_call)
                    if (rejectBtn != null) {
                        rejectBtn.setOnClickListener {
                            // Remove call data and finish activity
                            FirebaseDatabase.getInstance().getReference("calls").child(currentUid).removeValue()
                            finish()
                        }
                    }
                } else {
                    // No call data exists
                    Toast.makeText(this@CallRecieve, "Call no longer available", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CallRecieve, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure we don't leave hanging call references when activity is destroyed
        try {
            val currentUid = intent.getStringExtra("uid") ?:
            FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (currentUid.isNotEmpty()) {
                FirebaseDatabase.getInstance().getReference("calls").child(currentUid).removeValue()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}