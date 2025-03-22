package com.shaffinimam.i212963

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Requests : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FollowerAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val followersList = mutableListOf<FollowerModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FollowerAdapter(requireContext(), followersList)
        recyclerView.adapter = adapter

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        fetchFollowers()
    }

    private fun fetchFollowers() {
        val currentUserId = auth.currentUser?.uid ?: return

        database.child("followers").child(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    followersList.clear()
                    for (followerSnapshot in snapshot.children) {
                        val followerId = followerSnapshot.key ?: continue
                        fetchFollowerDetails(followerId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun fetchFollowerDetails(followerId: String) {
        val profileRef = FirebaseDatabase.getInstance().getReference("Profile").child(followerId)

        profileRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FirebaseCheck", "Snapshot: ${snapshot.value}")

                if (!snapshot.exists()) {
                    Log.e("FirebaseCheck", "No profile data found for $followerId")
                    return
                }

                val username = snapshot.child("UserName").getValue(String::class.java) ?: "Unknown"
                val profilePic = snapshot.child("Picture").getValue(String::class.java) ?: ""

                Log.d("FirebaseCheck", "Fetched Username: $username")
                Log.d("FirebaseCheck", "Fetched Picture Data: ${profilePic.take(30)}") // Limit log length

                followersList.add(FollowerModel(followerId, username, profilePic))
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseCheck", "Database error: ${error.message}")
            }
        })
    }

}

data class FollowerModel(val id: String, val username: String, val profilePic: String)

class FollowerAdapter(private val context: android.content.Context, private val followers: List<FollowerModel>) :
    RecyclerView.Adapter<FollowerAdapter.FollowerViewHolder>() {

    class FollowerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView = view.findViewById(R.id.username)
        val profilePic: ImageView = view.findViewById(R.id.profilePic)
        val acceptButton: Button = view.findViewById(R.id.accept_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_follower, parent, false)
        return FollowerViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowerViewHolder, position: Int) {
        val follower = followers[position]
        holder.username.text = follower.username

        // Decode Base64 and set image
        try {
            val bitmap = decodeBase64ToBitmap(follower.profilePic)
            if (bitmap != null) {
                holder.profilePic.setImageBitmap(bitmap)
            } else {
                holder.profilePic.setImageResource(R.drawable.prf) // Default image
            }
        } catch (e: IllegalArgumentException) {
            Log.e("Base64Error", "Invalid Base64 Image: ${e.message}")
            holder.profilePic.setImageResource(R.drawable.prf)
        }

        holder.acceptButton.setOnClickListener {
            addFollowing(follower.id) {
                Toast.makeText(context, "Request accepted successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun decodeBase64ToBitmap(encodedImage: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e("ImageDecode", "Error decoding image", e)
            null
        }
    }

    override fun getItemCount() = followers.size

    private fun addFollowing(followerId: String, onSuccess: () -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().reference.child("following")
        dbRef.child(currentUserId).child(followerId).setValue(true).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                Toast.makeText(context, "Failed to accept request", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
