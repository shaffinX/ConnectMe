package com.shaffinimam.i212963

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class Profile : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val userId get() = auth.currentUser?.uid ?: ""

    private lateinit var prfpic: ImageView
    private lateinit var usrname: TextView
    private lateinit var biotext: TextView
    private lateinit var text_followers: TextView
    private lateinit var text_following: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("Profile").child(userId)

        // Check if user is logged in
        if (userId.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            requireActivity().finish() // Close the activity
            return
        }

        val button = view.findViewById<ImageButton>(R.id.editpr)
        button.setOnClickListener {
            val intent = Intent(requireContext(), EditProfile::class.java)
            startActivity(intent)
        }

        val button2 = view.findViewById<LinearLayout>(R.id.follscr)
        val logout = view.findViewById<ImageButton>(R.id.logout)

        button2.setOnClickListener {
            val intent = Intent(requireContext(), FollowList::class.java)
            startActivity(intent)
        }
        prfpic = view.findViewById(R.id.prfpic)
        usrname = view.findViewById(R.id.usrname)
        biotext = view.findViewById(R.id.biotext)
        text_followers = view.findViewById(R.id.txtfols)
        text_following = view.findViewById(R.id.txtfolg)

        loadProfileData()
        logout.setOnClickListener {
            // Get Firebase Auth instance
            val auth = FirebaseAuth.getInstance()

            // Sign out the current user
            auth.signOut()

            // Display a message to the user
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Navigate back to the login screen
            val intent = Intent(requireContext(), Login::class.java)
            // Clear the activity stack so user can't go back to the app without logging in again
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // Close the current activity that contains this fragment
            requireActivity().finish()
        }



    }


    private fun loadProfileData() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val username = snapshot.child("UserName").getValue(String::class.java) ?: ""
                    val bio = snapshot.child("Bio").getValue(String::class.java) ?: ""
                    val pic = snapshot.child("Picture").getValue(String::class.java) ?: ""

                    val img = decodeImage(pic)
                    usrname.text = username
                    prfpic.setImageBitmap(img)
                    biotext.text = bio


                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
        database = FirebaseDatabase.getInstance().reference.child("users/").child(userId)
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val folr = snapshot.child("followers").getValue(String::class.java) ?: ""
                    val folg = snapshot.child("following").getValue(String::class.java) ?: ""


                    text_followers.text = folr
                    text_following.text = folg // Ensure this field is being displaye

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun decodeImage(encodedString: String): Bitmap {
        val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

}