package com.shaffinimam.i212963

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.jvm.java


class Home : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dm = view.findViewById<ImageButton>(R.id.dm)
        listenForIncomingCalls()
        dm.setOnClickListener {
            val intent = Intent(requireContext(), DM::class.java)
            startActivity(intent)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    private fun listenForIncomingCalls() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val callRef = FirebaseDatabase.getInstance().getReference("calls").child(currentUserUid)

        callRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val callerUid = snapshot.child("callerUid").getValue(String::class.java) ?: return
                    val channelName = snapshot.child("channelName").getValue(String::class.java) ?: return
                    val pict = snapshot.child("picture").getValue(String::class.java) ?: ""
                    val name = snapshot.child("name").getValue(String::class.java) ?: "Caller"

                    // Check if we're already in the CallRecieve activity to prevent duplicate intents
                    if (activity?.javaClass?.simpleName != "CallRecieve") {
                        val context = requireContext()
                        val intent = Intent(context, CallRecieve::class.java)
                        intent.putExtra("uid", currentUserUid)
                        intent.putExtra("channelName", channelName)
                        intent.putExtra("isCaller", false) // Receiver
                        intent.putExtra("pic", pict)
                        intent.putExtra("name", name)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }

                    // Don't remove the call data here - let the CallRecieve activity handle it
                    // snapshot.ref.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }



}