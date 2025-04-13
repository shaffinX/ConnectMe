package com.shaffinimam.i212963

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView

class DM2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dm2)

        val personUid = intent.getStringExtra("uid") ?: ""
        val pict = intent.getStringExtra("pic") ?: ""
        val name = intent.getStringExtra("name") ?: ""

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = name

        val profileImg = findViewById<CircleImageView>(R.id.profimg)

        // ✅ Decode and display profile picture
        if (pict.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(pict, Base64.DEFAULT)
                val decodedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                profileImg.setImageBitmap(decodedBitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error decoding profile image", Toast.LENGTH_SHORT).show()
            }
        }

        val cal = findViewById<ImageButton>(R.id.callpers)
        cal.setOnClickListener {
            val callerUid = FirebaseAuth.getInstance().currentUser?.uid

            if (callerUid.isNullOrEmpty()) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val channelName = "$callerUid-$personUid"
            val callRef = FirebaseDatabase.getInstance().getReference("calls").child(personUid)

            val callData = mapOf(
                "callerUid" to callerUid,
                "channelName" to channelName,
                "token" to "", // Fill if using a token (e.g., Agora)
                "picture" to pict,
                "name" to name
            )

            callRef.setValue(callData).addOnSuccessListener {
                // Start Call activity
                val intent = Intent(this, Call::class.java)
                intent.putExtra("uid", callerUid)
                intent.putExtra("channelName", channelName)
                intent.putExtra("isCaller", true)
                intent.putExtra("pic", pict)
                intent.putExtra("name", name)
                startActivity(intent)
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to send call signal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
