package com.shaffinimam.i212963

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Contact_All_Fragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: Adapter_profile
    private val profilesList = mutableListOf<UserProfileModel>()
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contact__all_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().reference
        recyclerView = view.findViewById(R.id.rv)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = Adapter_profile(requireContext(), profilesList)
        recyclerView.adapter = adapter

        if (currentUserUid.isNotEmpty()) {
            getSuggestedUsers { suggestedUsers ->
                profilesList.clear()
                profilesList.addAll(suggestedUsers)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun getSuggestedUsers(callback: (List<UserProfileModel>) -> Unit) {
        val usersList = mutableListOf<String>()
        val followingSet = mutableSetOf<String>()

        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    if (userId != currentUserUid) {
                        usersList.add(userId)
                    }
                }

                database.child("following").child(currentUserUid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(followingSnapshot: DataSnapshot) {
                            for (followerSnapshot in followingSnapshot.children) {
                                followingSet.add(followerSnapshot.key.toString())
                            }

                            val nonFollowedUids = usersList.filter { it !in followingSet }
                            getProfiles(nonFollowedUids, callback)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("FirebaseError", "Error: ${error.message}")
                        }
                    })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error: ${error.message}")
            }
        })
    }

    private fun getProfiles(userIds: List<String>, callback: (List<UserProfileModel>) -> Unit) {
        val profileList = mutableListOf<UserProfileModel>()

        if (userIds.isEmpty()) {
            callback(profileList)
            return
        }

        val profileRef = database.child("Profile")
        var count = 0

        for (uid in userIds) {
            profileRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("UserName").getValue(String::class.java) ?: "Unknown"
                    val picture = snapshot.child("Picture").getValue(String::class.java) ?: ""

                    profileList.add(UserProfileModel(uid, picture, username))

                    count++
                    if (count == userIds.size) {
                        callback(profileList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error: ${error.message}")
                }
            })
        }
    }
}
