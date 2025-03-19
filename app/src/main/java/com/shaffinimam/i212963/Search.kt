package com.shaffinimam.i212963

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class Search : Fragment() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private val profileList = mutableListOf<Profiles>()
    private val filteredList = mutableListOf<Profiles>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchEditText = view.findViewById(R.id.searchEditText)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        searchAdapter = SearchAdapter(filteredList)
        recyclerView.adapter = searchAdapter

        fetchAllProfiles()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().lowercase().trim()
                filteredList.clear()

                if (searchText.isEmpty()) {
                    filteredList.addAll(profileList)
                } else {
                    for (profile in profileList) {
                        if (profile.UserName.lowercase().contains(searchText)) {
                            filteredList.add(profile)
                        }
                    }
                }
                searchAdapter.notifyDataSetChanged()
            }
        })

        return view
    }

    private fun fetchAllProfiles() {
        databaseRef = FirebaseDatabase.getInstance().getReference("Profile")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FirebaseDebug", "Raw Data: $snapshot")  // Debugging

                profileList.clear()
                filteredList.clear()

                for (userSnapshot in snapshot.children) {
                    val profile = userSnapshot.getValue(Profiles::class.java)
                    Log.d("FirebaseDebug", "Profile Loaded: $profile")

                    if (profile != null) {
                        profileList.add(profile)
                    }
                }

                filteredList.addAll(profileList)
                searchAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching data: ${error.message}")
            }
        })
    }
}
