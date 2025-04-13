package com.shaffinimam.i212963

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DM_M : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: Adapter_dm
    private val userList = mutableListOf<Model_dm>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_d_m__m, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = Adapter_dm(requireContext(), userList)
        recyclerView.adapter = adapter

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val profilesRef = FirebaseDatabase.getInstance().getReference("Profile")
        profilesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (child in snapshot.children) {
                    val uid = child.key ?: continue
                    if (uid == currentUid) continue

                    val name = child.child("Name").getValue(String::class.java) ?: "Unknown"
                    val picture = child.child("Picture").getValue(String::class.java) ?: ""

                    userList.add(Model_dm(name, picture, uid))
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
