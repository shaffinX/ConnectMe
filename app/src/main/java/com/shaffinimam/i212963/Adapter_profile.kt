package com.shaffinimam.i212963

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class Adapter_profile(
    private val context: Context,
    private val profilesList: List<UserProfileModel>
) : RecyclerView.Adapter<Adapter_profile.ProfileViewHolder>() {

    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePic: ImageView = itemView.findViewById(R.id.profileImage)
        val username: TextView = itemView.findViewById(R.id.username)
        val button: Button = itemView.findViewById(R.id.folbutt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profile = profilesList[position]
        holder.username.text = profile.username

        if (profile.picture.isNotEmpty()) {
            val bitmap = decodeBase64ToBitmap(profile.picture)
            if (bitmap != null) {
                holder.profilePic.setImageBitmap(bitmap)  // Set decoded image
            } else {
                holder.profilePic.setImageResource(R.drawable.prf)  // Default image if decoding fails
            }
        } else {
            holder.profilePic.setImageResource(R.drawable.prf)  // Default image if no picture
        }

        // Handle Follow button click
        holder.button.setOnClickListener {
            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val followedPersonUid = profile.uid  // The user being followed

            val database = FirebaseDatabase.getInstance().reference
            val followRef = database.child("followers").child(followedPersonUid).child(currentUserUid)

            val followData = HashMap<String, Any>()
            followData["timestamp"] = System.currentTimeMillis()

            followRef.setValue(followData).addOnSuccessListener {
                holder.button.text = "Requested"  // Change button text after following
                holder.button.isEnabled = false  // Disable button after request
            }.addOnFailureListener { error ->
                Log.e("FirebaseError", "Follow request failed: ${error.message}")
            }
        }
    }


    override fun getItemCount(): Int = profilesList.size

    // Function to decode Base64 to Bitmap
    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str.replace("\n", ""), Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


}
