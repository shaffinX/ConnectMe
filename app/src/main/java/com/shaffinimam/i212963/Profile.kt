package com.shaffinimam.i212963

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth


class Profile : Fragment() {

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
}